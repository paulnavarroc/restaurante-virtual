package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.*;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.OrderService;
import com.saboresperu.restaurante.service.interfaces.PaymentService;
import com.saboresperu.restaurante.service.interfaces.PromotionEngine;
import com.saboresperu.restaurante.service.interfaces.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String STATUS_PENDING = "PENDING";
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderDiscountRepository orderDiscountRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final BranchRepository branchRepository;
    private final DeliveryTariffRepository deliveryTariffRepository;
    private final BranchConfigRepository branchConfigRepository;
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final PaymentService paymentService;
    private final DailyMenuItemRepository dailyMenuItemRepository;
    private final PromotionEngine promotionEngine;

    @Override
    @Transactional(readOnly = true)
    public CheckoutPreviewDto preview(Integer customerId, Integer addressId, String couponCode, CartDto cart) {
        if (cart == null || cart.isEmpty()) {
            return CheckoutPreviewDto.emptyCart();
        }

        try {
            BigDecimal subtotal = money(cart.getSubtotal());
            BigDecimal deliveryFee = ZERO;
            Integer branchId = null;

            if (addressId != null) {
                CustomerAddress address = requireAddress(customerId, addressId);
                Branch branch = resolveBranch(address);
                branchId = branch.getId();
                deliveryFee = calculateDeliveryFee(branch, address, subtotal);
            }

            List<AppliedPromotion> autos = branchId != null
                    ? promotionEngine.evaluateAutomatic(cart, branchId, subtotal, deliveryFee)
                    : List.of();

            BigDecimal promotionDiscount = autos.stream()
                    .map(AppliedPromotion::discountAmount)
                    .reduce(ZERO, BigDecimal::add);

            List<String> promoLabels = autos.stream()
                    .map(ap -> ap.promotion().getName() + " (−S/ "
                            + money(ap.discountAmount()) + ")")
                    .toList();

            BigDecimal couponDiscount = ZERO;
            String couponMessage = null;
            if (couponCode != null && !couponCode.isBlank()) {
                DiscountResult dr = applyCoupon(couponCode.trim(), subtotal, null);
                couponDiscount = dr.amount();
                couponMessage = dr.message();
            }

            BigDecimal discount = money(promotionDiscount.add(couponDiscount));
            BigDecimal total = money(subtotal.add(deliveryFee).subtract(discount).max(ZERO));

            return new CheckoutPreviewDto(
                    List.copyOf(cart.getItems()),
                    subtotal,
                    deliveryFee,
                    money(promotionDiscount),
                    money(couponDiscount),
                    discount,
                    total,
                    couponMessage,
                    promoLabels,
                    true,
                    null
            );
        } catch (RuntimeException ex) {
            BigDecimal subtotal = money(cart.getSubtotal());
            return new CheckoutPreviewDto(
                    List.copyOf(cart.getItems()),
                    subtotal,
                    ZERO,
                    ZERO,
                    ZERO,
                    ZERO,
                    subtotal,
                    null,
                    List.of(),
                    false,
                    ex.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public Order placeOrder(Integer customerId, CheckoutRequestDto request, CartDto cart, Integer userId) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        CustomerAddress address = requireAddress(customerId, request.addressId());
        Branch branch = resolveBranch(address);
        OrderStatus pending = orderStatusRepository.findByCode(STATUS_PENDING)
                .orElseThrow(() -> new IllegalStateException("Order status PENDING not found"));

        BigDecimal subtotal = money(cart.getSubtotal());
        BigDecimal deliveryFee = calculateDeliveryFee(branch, address, subtotal);

        DiscountResult couponResult = DiscountResult.none();
        Coupon coupon = null;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            couponResult = applyCoupon(request.couponCode().trim(), subtotal, null);
            if (couponResult.couponId() != null) {
                coupon = couponRepository.findById(couponResult.couponId()).orElse(null);
            }
        }

        List<AppliedPromotion> autos = promotionEngine.evaluateAutomatic(cart, branch.getId(), subtotal, deliveryFee);

        BigDecimal promoDiscount = autos.stream()
                .map(AppliedPromotion::discountAmount)
                .reduce(ZERO, BigDecimal::add);

        // cupón (si existe) se suma después o con reglas de no stack — simplificado: suma
        BigDecimal couponDiscount = couponResult.amount();
        BigDecimal discount = money(promoDiscount.add(couponDiscount));
        BigDecimal total = money(subtotal.add(deliveryFee).subtract(discount).max(ZERO));


        BranchConfig config = branchConfigRepository.findById(branch.getId()).orElse(null);
        if (config != null && config.getMinOrderAmount() != null
                && subtotal.compareTo(config.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException(
                    "El pedido mínimo es S/ " + money(config.getMinOrderAmount()));
        }

        Customer customerRef = address.getCustomer();

        Order order = Order.builder()
                .customer(customerRef)
                .branch(branch)
                .address(address)
                .orderStatus(pending)
                .orderedAt(LocalDateTime.now())
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .discount(discount)
                .total(total)
                .notes(blankToNull(request.notes()))
                .couponCode(coupon != null ? coupon.getCode() : blankToNull(request.couponCode()))
                .items(new ArrayList<>())
                .build();

        for (CartItemDto line : cart.getItems()) {
            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado: " + line.getProductId()));

            DailyMenuItem dmi = null;
            if (line.getDailyMenuItemId() != null) {
                dmi = dailyMenuItemRepository.findById(line.getDailyMenuItemId())
                        .orElseThrow(() -> new IllegalArgumentException("Ítem de menú no encontrado"));
                if (!Boolean.TRUE.equals(dmi.getIsAvailable())) {
                    throw new IllegalArgumentException("No disponible: " + product.getName());
                }
                int sold = dmi.getSoldQuantity() != null ? dmi.getSoldQuantity() : 0;
                Integer max = dmi.getMaxQuota();
                if (max != null && sold + line.getQuantity() > max) {
                    throw new IllegalArgumentException("Cupo insuficiente: " + product.getName());
                }
            }

            BigDecimal unit = money(line.getUnitPrice()); // precio del carrito (override)
            int qty = line.getQuantity();
            BigDecimal lineTotal = money(unit.multiply(BigDecimal.valueOf(qty)));

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .dailyMenuItem(dmi)
                    .productNameSnapshot(product.getName())
                    .unitPrice(unit)
                    .quantity(qty)
                    .lineDiscount(ZERO)
                    .lineTotal(lineTotal)
                    .build();
            order.getItems().add(item);
        }

        order = orderRepository.save(order);

        for (CartItemDto line : cart.getItems()) {
            if (line.getDailyMenuItemId() == null) {
                continue;
            }
            int updated = dailyMenuItemRepository.incrementSoldIfAvailable(line.getDailyMenuItemId(), line.getQuantity());
            if (updated == 0) {
                Product product = productRepository.findById(line.getProductId()).orElse(null);
                String name = product != null ? product.getName() : "producto";
                throw new IllegalArgumentException("Cupo agotado o no disponible: " + name);
            }
        }

        order.getItems().forEach(i -> {
            if (i.getProduct() != null) {
                i.getProduct().getId();
                i.getProduct().getIsUnlimitedStock();
            }
        });

        stockService.reserveForOrder(order);

        orderHistoryRepository.save(OrderHistory.builder()
                .order(order)
                .orderStatus(pending)
                .notes("Pedido creado")
                .createdAt(LocalDateTime.now())
                .build());

        if (coupon != null && couponDiscount.compareTo(ZERO) > 0) {
            orderDiscountRepository.save(OrderDiscount.builder()
                    .order(order)
                    .sourceType("COUPON")
                    .coupon(coupon)
                    .codeSnapshot(coupon.getCode())
                    .nameSnapshot(coupon.getDescription() != null ? coupon.getDescription() : coupon.getCode())
                    .discountAmount(couponDiscount)
                    .build());

            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);

            couponUsageRepository.save(CouponUsage.builder()
                    .coupon(coupon)
                    .user(customerRef.getUser())
                    .order(order)
                    .usedAt(LocalDateTime.now())
                    .build());
        }

        for (AppliedPromotion ap : autos) {
            orderDiscountRepository.save(OrderDiscount.builder()
                    .order(order)
                    .sourceType("PROMOTION")
                    .promotion(ap.promotion())
                    .codeSnapshot(ap.promotion().getCode())
                    .nameSnapshot(ap.promotion().getName())
                    .discountAmount(ap.discountAmount())
                    .build());
        }

        paymentService.registerForOrder(order, request.paymentMethodId(), userId);
        return order;
    }

    @Override
    @Transactional
    public void cancelOrder(Integer orderId, Integer customerId, String reason) {
        Order order = getByIdAndCustomerId(orderId, customerId);
        String code = order.getOrderStatus().getCode();
        if ("DELIVERED".equals(code) || "CANCELLED".equals(code)) {
            throw new IllegalArgumentException("No se puede cancelar este pedido");
        }

        OrderStatus cancelled = orderStatusRepository.findByCode("CANCELLED")
                .orElseThrow(() -> new IllegalStateException("Status CANCELLED not found"));

        stockService.releaseReservations(order);

        order.setOrderStatus(cancelled);
        orderRepository.save(order);

        orderHistoryRepository.save(OrderHistory.builder()
                .order(order)
                .orderStatus(cancelled)
                .notes(reason != null ? reason : "Pedido cancelado")
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> listByCustomerId(Integer customerId) {
        return orderRepository.findByCustomerIdWithStatus(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getByIdAndCustomerId(Integer orderId, Integer customerId) {
        return orderRepository.findByIdAndCustomerIdWithDetails(orderId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
    }

    private CustomerAddress requireAddress(Integer customerId, Integer addressId) {
        return customerAddressRepository
                .findByIdAndCustomerIdWithDistrict(addressId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Dirección no válida"));
    }

    /** Primera sede activa; luego se puede elegir por distancia/cobertura. */
    private Branch resolveBranch(CustomerAddress address) {
        return branchRepository.findByIsActiveTrue().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay sedes activas"));
    }

    private BigDecimal calculateDeliveryFee(Branch branch, CustomerAddress address, BigDecimal subtotal) {
        BranchConfig config = branchConfigRepository.findById(branch.getId()).orElse(null);
        if (config != null && config.getFreeDeliveryFromAmount() != null
                && subtotal.compareTo(config.getFreeDeliveryFromAmount()) >= 0) {
            return ZERO;
        }

        List<DeliveryTariff> tariffs =
                deliveryTariffRepository.findByBranchIdAndIsActiveTrueOrderBySortOrderAsc(branch.getId());

        // Distrito
        if (address.getDistrict() != null) {
            for (DeliveryTariff t : tariffs) {
                if ("DISTRICT".equals(t.getRuleType())
                        && t.getDistrict() != null
                        && t.getDistrict().getId().equals(address.getDistrict().getId())) {
                    return money(t.getFee());
                }
            }
        }

        // Distancia (si hay coords)
        if (address.getLat() != null && address.getLng() != null
                && branch.getLat() != null && branch.getLng() != null) {
            double km = haversineKm(
                    branch.getLat().doubleValue(), branch.getLng().doubleValue(),
                    address.getLat().doubleValue(), address.getLng().doubleValue());
            for (DeliveryTariff t : tariffs) {
                if (!"DISTANCE_KM".equals(t.getRuleType())) continue;
                double min = t.getMinDistanceKm() != null ? t.getMinDistanceKm().doubleValue() : 0;
                double max = t.getMaxDistanceKm() != null ? t.getMaxDistanceKm().doubleValue() : Double.MAX_VALUE;
                if (km >= min && km < max) {
                    return money(t.getFee());
                }
            }
        }

        if (config != null && config.getDefaultDeliveryFee() != null) {
            return money(config.getDefaultDeliveryFee());
        }
        return money(new BigDecimal("8.00"));
    }

    private DiscountResult applyCoupon(String code, BigDecimal subtotal, Integer userId) {
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new IllegalArgumentException("Cupón no válido"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartsAt()) || now.isAfter(coupon.getEndsAt())) {
            throw new IllegalArgumentException("El cupón está fuera de vigencia");
        }
        if (coupon.getMaxUses() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new IllegalArgumentException("El cupón ya no tiene usos disponibles");
        }
        if (coupon.getMinOrderAmount() != null && subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException("No alcanzas el monto mínimo del cupón");
        }

        BigDecimal amount;
        if ("PERCENT".equals(coupon.getDiscountType())) {
            amount = subtotal.multiply(coupon.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            amount = money(coupon.getDiscountValue());
        }
        if (amount.compareTo(subtotal) > 0) {
            amount = subtotal;
        }
        return new DiscountResult(amount, "Cupón aplicado: " + coupon.getCode(), coupon.getId());
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static BigDecimal money(BigDecimal value) {
        if (value == null) return ZERO;
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    private record DiscountResult(BigDecimal amount, String message, Integer couponId) {
        static DiscountResult none() {
            return new DiscountResult(ZERO, null, null);
        }
    }

    private PricingBreakdown price(CartDto cart, Branch branch, CustomerAddress address,
                                   String couponCode) {
        BigDecimal subtotal = money(cart.getSubtotal());
        BigDecimal deliveryFee = address != null
                ? calculateDeliveryFee(branch, address, subtotal)
                : ZERO;

        List<AppliedPromotion> autos = branch != null
                ? promotionEngine.evaluateAutomatic(cart, branch.getId(), subtotal, deliveryFee)
                : List.of();

        BigDecimal promotionDiscount = autos.stream()
                .map(AppliedPromotion::discountAmount)
                .reduce(ZERO, BigDecimal::add);

        DiscountResult couponResult = DiscountResult.none();
        if (couponCode != null && !couponCode.isBlank()) {
            couponResult = applyCoupon(couponCode.trim(), subtotal, null);
        }

        BigDecimal couponDiscount = couponResult.amount();
        BigDecimal discount = money(promotionDiscount.add(couponDiscount));
        BigDecimal total = money(subtotal.add(deliveryFee).subtract(discount).max(ZERO));

        return new PricingBreakdown(
                subtotal, deliveryFee,
                money(promotionDiscount), money(couponDiscount),
                discount, total, autos, couponResult);
    }

    private record PricingBreakdown(
            BigDecimal subtotal,
            BigDecimal deliveryFee,
            BigDecimal promotionDiscount,
            BigDecimal couponDiscount,
            BigDecimal discount,
            BigDecimal total,
            List<AppliedPromotion> promotions,
            DiscountResult couponResult
    ) {}
}
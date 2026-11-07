package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.OrderStatusCodes;
import com.saboresperu.restaurante.dto.*;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.AdminOrderService;
import com.saboresperu.restaurante.service.interfaces.DeliveryService;
import com.saboresperu.restaurante.service.interfaces.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final StockService stockService;
    private final DeliveryService deliveryService;

    @Override
    @Transactional(readOnly = true)
    public List<AdminOrderRowDto> search(String statusCode, Integer branchId, int limit) {
        String status = (statusCode == null || statusCode.isBlank()) ? null : statusCode;
        return orderRepository.searchAdmin(status, branchId, PageRequest.of(0, limit)).stream()
                .map(this::toRow)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderDetailDto getDetail(Integer orderId) {
        Order o = orderRepository.findByIdForAdmin(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        List<AdminOrderItemDto> items = o.getItems() == null ? List.of() : o.getItems().stream()
                .map(i -> new AdminOrderItemDto(
                        i.getProductNameSnapshot(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getLineDiscount(),
                        i.getLineTotal()
                ))
                .toList();

        List<AdminOrderPaymentDto> payments = paymentRepository.findByOrderIdWithMethod(orderId).stream()
                .map(p -> new AdminOrderPaymentDto(
                        p.getPaymentMethod().getName(),
                        p.getStatus(),
                        p.getAmount(),
                        p.getPaidAt() != null ? p.getPaidAt().format(FMT) : null
                ))
                .toList();

        List<AdminOrderHistoryDto> history = orderHistoryRepository.findByOrderIdWithDetails(orderId).stream()
                .map(h -> new AdminOrderHistoryDto(
                        h.getOrderStatus().getName(),
                        h.getNotes(),
                        h.getUser() != null
                                ? h.getUser().getFirstName() + " " + h.getUser().getLastName()
                                : null,
                        h.getCreatedAt() != null ? h.getCreatedAt().format(FMT) : null
                ))
                .toList();

        User cu = o.getCustomer().getUser();
        String addressLine = o.getAddress().getStreet() + (o.getAddress().getNumber() != null ? " " + o.getAddress().getNumber() : "");
        String district = o.getAddress().getDistrict() != null ? o.getAddress().getDistrict().getName() : null;

        return new AdminOrderDetailDto(
                o.getId(),
                o.getOrderStatus().getCode(),
                o.getOrderStatus().getName(),
                (cu.getFirstName() + " " + cu.getLastName()).trim(),
                cu.getEmail(),
                o.getBranch().getName(),
                addressLine,
                district,
                o.getSubtotal(),
                o.getDeliveryFee(),
                o.getDiscount(),
                o.getTotal(),
                o.getNotes(),
                o.getCouponCode(),
                o.getOrderedAt() != null ? o.getOrderedAt().format(FMT) : null,
                items,
                payments,
                history
        );
    }

    @Override
    @Transactional
    public void updateStatus(Integer orderId, String newStatusCode, Integer userId) {
        Order order = orderRepository.findByIdForAdmin(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        String from = order.getOrderStatus().getCode();
        if (from.equals(newStatusCode)) {
            return;
        }

        OrderStatus next = orderStatusRepository.findByCode(newStatusCode)
                .orElseThrow(() -> new IllegalArgumentException("Estado inválido"));

        // Cancelación: liberar stock si aún hay reservas
        if (OrderStatusCodes.CANCELLED.equals(newStatusCode)) {
            stockService.releaseReservations(order);
        }

        // A preparación: consumir reservas
        if (OrderStatusCodes.IN_PREPARATION.equals(newStatusCode)
                && !OrderStatusCodes.IN_PREPARATION.equals(from)) {
            try {
                stockService.consumeReservations(order);
            } catch (Exception ignored) {
                // si no había reservas, continuar
            }
        }

        order.setOrderStatus(next);
        orderRepository.save(order);

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        orderHistoryRepository.save(OrderHistory.builder()
                .order(order)
                .orderStatus(next)
                .user(user)
                .notes("Admin: " + from + " → " + newStatusCode)
                .createdAt(java.time.LocalDateTime.now())
                .build());

        if (OrderStatusCodes.READY.equals(newStatusCode)) {
            deliveryService.ensurePendingDelivery(order);
        }
    }

    private AdminOrderRowDto toRow(Order o) {
        User u = o.getCustomer().getUser();
        String paymentStatus = paymentRepository.findByOrderIdWithMethod(o.getId()).stream()
                .findFirst()
                .map(Payment::getStatus)
                .orElse("—");

        return new AdminOrderRowDto(
                o.getId(),
                o.getOrderStatus().getCode(),
                o.getOrderStatus().getName(),
                (u.getFirstName() + " " + u.getLastName()).trim(),
                u.getEmail(),
                o.getBranch().getName(),
                o.getTotal(),
                paymentStatus,
                o.getOrderedAt() != null ? o.getOrderedAt().format(FMT) : null
        );
    }
}
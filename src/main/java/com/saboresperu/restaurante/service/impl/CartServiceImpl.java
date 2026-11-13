package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.CartSessionKeys;
import com.saboresperu.restaurante.dto.CartDto;
import com.saboresperu.restaurante.dto.CartItemDto;
import com.saboresperu.restaurante.dto.CartSanitizeResult;
import com.saboresperu.restaurante.entity.CartItem;
import com.saboresperu.restaurante.entity.DailyMenu;
import com.saboresperu.restaurante.entity.DailyMenuItem;
import com.saboresperu.restaurante.entity.Product;
import com.saboresperu.restaurante.repository.CartItemRepository;
import com.saboresperu.restaurante.repository.DailyMenuItemRepository;
import com.saboresperu.restaurante.repository.DailyMenuRepository;
import com.saboresperu.restaurante.repository.ProductRepository;
import com.saboresperu.restaurante.service.interfaces.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ProductRepository productRepository;
    private final DailyMenuItemRepository dailyMenuItemRepository;
    private final DailyMenuRepository dailyMenuRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public CartDto getCart(HttpSession session) {
        Object raw = session.getAttribute(CartSessionKeys.CART);
        if (raw instanceof CartDto cart) {
            return cart;
        }
        CartDto cart = new CartDto();
        session.setAttribute(CartSessionKeys.CART, cart);
        return cart;
    }

    @Override
    @Transactional(readOnly = true)
    public void addItem(HttpSession session, Integer productId, Integer dailyMenuItemId, int quantity) {
        if (quantity < 1) {
            quantity = 1;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new IllegalArgumentException("El producto no está disponible");
        }

        BigDecimal unitPrice = money(product.getBasePrice());
        Integer resolvedDmiId = null;

        if (dailyMenuItemId != null) {
            DailyMenuItem dmi = dailyMenuItemRepository.findById(dailyMenuItemId)
                    .orElseThrow(() -> new IllegalArgumentException("Ítem de menú no encontrado"));
            if (!dmi.getProduct().getId().equals(productId)) {
                throw new IllegalArgumentException("El producto no coincide con el menú del día");
            }
            assertMenuItemSellable(dmi, quantity);
            if (dmi.getPriceOverride() != null) {
                unitPrice = money(dmi.getPriceOverride());
            }
            resolvedDmiId = dmi.getId();
        }

        CartDto cart = getCart(session);
        CartItemDto existing = findLine(cart, productId, resolvedDmiId);

        if (existing != null) {
            int newQty = existing.getQuantity() + quantity;
            if (resolvedDmiId != null) {
                DailyMenuItem dmi = dailyMenuItemRepository.findById(resolvedDmiId).orElseThrow();
                assertMenuItemSellable(dmi, newQty);
            }
            existing.setQuantity(newQty);
            existing.setUnitPrice(unitPrice);
        } else {
            cart.getItems().add(CartItemDto.builder()
                    .productId(product.getId())
                    .dailyMenuItemId(resolvedDmiId)
                    .productName(product.getName())
                    .imageUrl(product.getImageUrl())
                    .unitPrice(unitPrice)
                    .quantity(quantity)
                    .build());
        }
        session.setAttribute(CartSessionKeys.CART, cart);
    }

    @Override
    @Transactional(readOnly = true)
    public void updateQuantity(HttpSession session, Integer productId, Integer dailyMenuItemId, int quantity) {
        if (quantity <= 0) {
            removeItem(session, productId, dailyMenuItemId);
            return;
        }
        CartDto cart = getCart(session);
        CartItemDto line = findLine(cart, productId, dailyMenuItemId);
        if (line == null) {
            return;
        }
        if (line.getDailyMenuItemId() != null) {
            DailyMenuItem dmi = dailyMenuItemRepository.findById(line.getDailyMenuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Ítem de menú no encontrado"));
            assertMenuItemSellable(dmi, quantity);
        }
        line.setQuantity(quantity);
        session.setAttribute(CartSessionKeys.CART, cart);
    }

    @Override
    public void removeItem(HttpSession session, Integer productId, Integer dailyMenuItemId) {
        CartDto cart = getCart(session);
        cart.getItems().removeIf(i -> sameLine(i, productId, dailyMenuItemId));
        session.setAttribute(CartSessionKeys.CART, cart);
    }

    @Override
    public void clear(HttpSession session) {
        session.removeAttribute(CartSessionKeys.CART);
    }

    @Override
    public int countItems(HttpSession session) {
        return getCart(session).getTotalQuantity();
    }

    @Override
    @Transactional
    public CartSanitizeResult mergeAndLoadForCustomer(HttpSession session, Integer customerId, Integer branchId) {
        CartDto sessionCart = copyCart(getCart(session));
        CartDto dbCart = loadFromDb(customerId);

        CartDto merged = new CartDto();
        for (CartItemDto line : dbCart.getItems()) {
            merged.getItems().add(copyItem(line));
        }
        for (CartItemDto s : sessionCart.getItems()) {
            CartItemDto existing = findLine(merged, s.getProductId(), s.getDailyMenuItemId());
            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + s.getQuantity());
            } else {
                merged.getItems().add(copyItem(s));
            }
        }

        CartSanitizeResult result = sanitizeAgainstTodayMenu(merged, branchId);
        session.setAttribute(CartSessionKeys.CART, result.cart());
        saveToDb(customerId, result.cart());
        return result;
    }

    @Override
    @Transactional
    public CartSanitizeResult refreshForCustomer(HttpSession session, Integer customerId, Integer branchId) {
        // Preferir sesión; si está vacía, cargar BD
        CartDto cart = getCart(session);
        if (cart.isEmpty()) {
            cart = loadFromDb(customerId);
        }
        CartSanitizeResult result = sanitizeAgainstTodayMenu(cart, branchId);
        session.setAttribute(CartSessionKeys.CART, result.cart());
        saveToDb(customerId, result.cart());
        return result;
    }

    @Transactional
    public void persistSessionCart(Integer customerId, HttpSession session) {
        saveToDb(customerId, getCart(session));
    }

    @Transactional
    public void clearPersisted(Integer customerId, HttpSession session) {
        clear(session);
        cartItemRepository.deleteByCustomerId(customerId);
    }

    private CartSanitizeResult sanitizeAgainstTodayMenu(CartDto cart, Integer branchId) {
        List<String> removed = new ArrayList<>();
        List<String> adjusted = new ArrayList<>();

        if (cart == null || cart.isEmpty()) {
            return new CartSanitizeResult(cart != null ? cart : new CartDto(), List.of(), List.of());
        }

        if (branchId == null) {
            // Sin sede no podemos validar menú: dejar como está o vaciar líneas con DMI
            return new CartSanitizeResult(cart, List.of(), List.of());
        }

        LocalDate today = LocalDate.now();
        Optional<DailyMenu> menuOpt = dailyMenuRepository.findPublishedWithProducts(branchId, today);

        if (menuOpt.isEmpty()) {
            // No hay menú publicado hoy → quitar todo lo ligado a menú del día
            CartDto empty = new CartDto();
            for (CartItemDto line : cart.getItems()) {
                if (line.getDailyMenuItemId() != null) {
                    removed.add(line.getProductName());
                } else {
                    empty.getItems().add(copyItem(line));
                }
            }
            return new CartSanitizeResult(empty, removed, adjusted);
        }

        Map<Integer, DailyMenuItem> byDmiId = menuOpt.get().getItems().stream()
                .filter(i -> i.getId() != null)
                .collect(Collectors.toMap(DailyMenuItem::getId, Function.identity(), (a, b) -> a));

        CartDto kept = new CartDto();
        for (CartItemDto line : cart.getItems()) {
            if (line.getDailyMenuItemId() == null) {
                // Producto fuera de menú diario: opcional; aquí se mantiene si el producto sigue activo
                productRepository.findById(line.getProductId()).ifPresentOrElse(p -> {
                    if (Boolean.TRUE.equals(p.getIsActive())) {
                        line.setProductName(p.getName());
                        line.setImageUrl(p.getImageUrl());
                        line.setUnitPrice(money(p.getBasePrice()));
                        kept.getItems().add(line);
                    } else {
                        removed.add(line.getProductName());
                    }
                }, () -> removed.add(line.getProductName()));
                continue;
            }

            DailyMenuItem dmi = byDmiId.get(line.getDailyMenuItemId());
            if (dmi == null || !Boolean.TRUE.equals(dmi.getIsAvailable())) {
                removed.add(line.getProductName());
                continue;
            }

            int sold = dmi.getSoldQuantity() != null ? dmi.getSoldQuantity() : 0;
            Integer max = dmi.getMaxQuota();
            int remaining = max == null ? Integer.MAX_VALUE : Math.max(0, max - sold);
            if (remaining <= 0) {
                removed.add(line.getProductName());
                continue;
            }

            if (line.getQuantity() > remaining) {
                line.setQuantity(remaining);
                adjusted.add(line.getProductName());
            }

            BigDecimal price = dmi.getPriceOverride() != null
                    ? money(dmi.getPriceOverride())
                    : money(dmi.getProduct().getBasePrice());
            line.setUnitPrice(price);
            line.setProductName(dmi.getProduct().getName());
            line.setImageUrl(dmi.getProduct().getImageUrl());
            line.setProductId(dmi.getProduct().getId());
            kept.getItems().add(line);
        }

        return new CartSanitizeResult(kept, removed, adjusted);
    }

    private void saveToDb(Integer customerId, CartDto cart) {
        cartItemRepository.deleteByCustomerId(customerId);
        if (cart == null || cart.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (CartItemDto line : cart.getItems()) {
            cartItemRepository.save(CartItem.builder()
                    .customerId(customerId)
                    .productId(line.getProductId())
                    .dailyMenuItemId(line.getDailyMenuItemId())
                    .quantity(line.getQuantity())
                    .unitPrice(money(line.getUnitPrice()))
                    .productName(line.getProductName() != null ? line.getProductName() : "Producto")
                    .imageUrl(line.getImageUrl())
                    .updatedAt(now)
                    .build());
        }
    }

    private CartDto loadFromDb(Integer customerId) {
        CartDto cart = new CartDto();
        for (CartItem e : cartItemRepository.findByCustomerIdOrderByUpdatedAtDesc(customerId)) {
            cart.getItems().add(CartItemDto.builder()
                    .productId(e.getProductId())
                    .dailyMenuItemId(e.getDailyMenuItemId())
                    .productName(e.getProductName())
                    .imageUrl(e.getImageUrl())
                    .unitPrice(e.getUnitPrice())
                    .quantity(e.getQuantity())
                    .build());
        }
        return cart;
    }
    
    private void assertMenuItemSellable(DailyMenuItem dmi, int qtyWanted) {
        if (!Boolean.TRUE.equals(dmi.getIsAvailable())) {
            throw new IllegalArgumentException("Producto no disponible en el menú de hoy");
        }
        Integer max = dmi.getMaxQuota();
        int sold = dmi.getSoldQuantity() != null ? dmi.getSoldQuantity() : 0;
        if (max != null && sold + qtyWanted > max) {
            int left = Math.max(0, max - sold);
            throw new IllegalArgumentException(
                    left == 0 ? "Cupo agotado" : "Solo quedan " + left + " unidades");
        }
    }

    private static CartItemDto findLine(CartDto cart, Integer productId, Integer dailyMenuItemId) {
        return cart.getItems().stream()
                .filter(i -> sameLine(i, productId, dailyMenuItemId))
                .findFirst()
                .orElse(null);
    }

    private static boolean sameLine(CartItemDto i, Integer productId, Integer dailyMenuItemId) {
        return Objects.equals(i.getProductId(), productId)
                && Objects.equals(i.getDailyMenuItemId(), dailyMenuItemId);
    }

    private static CartDto copyCart(CartDto src) {
        CartDto c = new CartDto();
        if (src != null) {
            for (CartItemDto i : src.getItems()) {
                c.getItems().add(copyItem(i));
            }
        }
        return c;
    }

    private static CartItemDto copyItem(CartItemDto i) {
        return CartItemDto.builder()
                .productId(i.getProductId())
                .dailyMenuItemId(i.getDailyMenuItemId())
                .productName(i.getProductName())
                .imageUrl(i.getImageUrl())
                .unitPrice(i.getUnitPrice())
                .quantity(i.getQuantity())
                .build();
    }

    private static BigDecimal money(BigDecimal v) {
        if (v == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
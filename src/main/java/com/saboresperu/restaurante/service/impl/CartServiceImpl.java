package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.CartSessionKeys;
import com.saboresperu.restaurante.dto.CartDto;
import com.saboresperu.restaurante.dto.CartItemDto;
import com.saboresperu.restaurante.entity.DailyMenuItem;
import com.saboresperu.restaurante.entity.Product;
import com.saboresperu.restaurante.repository.DailyMenuItemRepository;
import com.saboresperu.restaurante.repository.ProductRepository;
import com.saboresperu.restaurante.service.interfaces.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ProductRepository productRepository;
    private final DailyMenuItemRepository dailyMenuItemRepository;

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

    private static BigDecimal money(BigDecimal v) {
        if (v == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
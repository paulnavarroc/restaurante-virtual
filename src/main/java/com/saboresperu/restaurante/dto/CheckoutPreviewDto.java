package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutPreviewDto(
        List<CartItemDto> items,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal promotionDiscount,
        BigDecimal couponDiscount,
        BigDecimal discount,
        BigDecimal total,
        String couponMessage,
        List<String> promotionLabels,
        boolean valid,
        String errorMessage
) {
    public static CheckoutPreviewDto emptyCart() {
        return new CheckoutPreviewDto(
                List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, null, List.of(), false, "El carrito está vacío"
        );
    }
}
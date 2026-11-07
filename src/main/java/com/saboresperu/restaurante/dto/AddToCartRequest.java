package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotNull Integer productId,
        Integer dailyMenuItemId,
        @Min(1) int quantity
) {
    public AddToCartRequest {
        if (quantity < 1) {
            quantity = 1;
        }
    }
}
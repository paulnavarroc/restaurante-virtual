package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockAdjustRequest(
        @NotNull Integer branchId,
        @NotNull Integer ingredientId,
        @NotNull BigDecimal newAvailableQuantity,
        String notes
) {}
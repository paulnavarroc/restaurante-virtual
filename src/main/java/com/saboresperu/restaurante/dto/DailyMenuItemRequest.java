package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DailyMenuItemRequest(
        @NotNull Integer productId,
        BigDecimal priceOverride,
        Integer maxQuota,
        boolean available
) {}
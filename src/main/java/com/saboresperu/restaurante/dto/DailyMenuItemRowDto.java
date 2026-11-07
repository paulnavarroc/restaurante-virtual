package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;
import java.util.List;

public record DailyMenuItemRowDto(
        Integer id,
        Integer productId,
        String productName,
        BigDecimal basePrice,
        BigDecimal priceOverride,
        BigDecimal effectivePrice,
        Integer maxQuota,
        int soldQuantity,
        boolean available,
        List<String> shiftLabels  // vacío = todos
) {}
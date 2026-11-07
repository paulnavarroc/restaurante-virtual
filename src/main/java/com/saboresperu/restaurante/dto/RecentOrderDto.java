package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;

public record RecentOrderDto(
        Integer id,
        String statusName,
        String statusCode,
        BigDecimal total,
        String customerName,
        String orderedAtLabel
) {}
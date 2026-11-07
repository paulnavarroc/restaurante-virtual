package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;

public record AdminOrderItemDto(
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineDiscount,
        BigDecimal lineTotal
) {}
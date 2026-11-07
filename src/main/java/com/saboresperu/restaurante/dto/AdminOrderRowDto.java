package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;

public record AdminOrderRowDto(
        Integer id,
        String statusCode,
        String statusName,
        String customerName,
        String customerEmail,
        String branchName,
        BigDecimal total,
        String paymentStatus,
        String orderedAtLabel
) {}
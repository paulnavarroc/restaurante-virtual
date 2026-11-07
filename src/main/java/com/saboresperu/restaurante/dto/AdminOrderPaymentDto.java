package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;

public record AdminOrderPaymentDto(
        String methodName,
        String status,
        BigDecimal amount,
        String paidAtLabel
) {}
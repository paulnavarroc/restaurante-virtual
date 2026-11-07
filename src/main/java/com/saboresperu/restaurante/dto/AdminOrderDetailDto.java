package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminOrderDetailDto(
        Integer id,
        String statusCode,
        String statusName,
        String customerName,
        String customerEmail,
        String branchName,
        String addressLine,
        String districtName,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal discount,
        BigDecimal total,
        String notes,
        String couponCode,
        String orderedAtLabel,
        List<AdminOrderItemDto> items,
        List<AdminOrderPaymentDto> payments,
        List<AdminOrderHistoryDto> history
) {}
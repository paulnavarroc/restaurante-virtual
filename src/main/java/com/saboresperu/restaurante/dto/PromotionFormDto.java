package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PromotionFormDto(
        Integer id,
        @Size(max = 40) String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        @NotBlank String promoType,      // PERCENT | AMOUNT
        BigDecimal discountPercent,
        BigDecimal discountAmount,
        @NotBlank String scope,          // PRODUCT | CATEGORY | ORDER | DELIVERY
        @NotNull LocalDateTime startsAt,
        @NotNull LocalDateTime endsAt,
        @NotNull Integer priority,
        boolean stackable,
        boolean automatic,
        BigDecimal minOrderAmount,
        BigDecimal maxDiscountAmount,
        Integer branchId,                // null = todas
        boolean active,
        List<Integer> productIds,
        List<Integer> categoryIds
) {}
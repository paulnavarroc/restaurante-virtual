package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponRowDto(
        Integer id,
        String code,
        String description,
        String discountType,
        BigDecimal discountValue,
        Integer maxUses,
        int usedCount,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        boolean active
) {}
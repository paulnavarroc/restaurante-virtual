package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponFormDto(
        Integer id,
        @NotBlank @Size(max = 40) String code,
        @Size(max = 300) String description,
        @NotBlank String discountType,   // PERCENT | AMOUNT
        @NotNull @DecimalMin("0.01") BigDecimal discountValue,
        BigDecimal minOrderAmount,
        Integer maxUses,
        @NotNull LocalDateTime startsAt,
        @NotNull LocalDateTime endsAt,
        boolean active
) {}
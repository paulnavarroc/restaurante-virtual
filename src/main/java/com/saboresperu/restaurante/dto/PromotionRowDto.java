package com.saboresperu.restaurante.dto;

import java.time.LocalDateTime;

public record PromotionRowDto(
        Integer id,
        String code,
        String name,
        String promoType,
        String scope,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        boolean automatic,
        boolean active
) {}
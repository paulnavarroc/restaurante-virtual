package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;

public record ProductCardDto(
        Integer id,
        Integer dailyMenuItemId,
        String name,
        String description,
        BigDecimal listPrice,
        BigDecimal salePrice,
        boolean onPromoPrice,
        String imageUrl,
        String categoryName,
        String regionName,
        Integer prepTimeMinutes,
        boolean available,
        boolean soldOut,
        Integer remainingQuota
) {}
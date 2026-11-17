package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductCardDto(
        Integer id,
        Integer dailyMenuItemId,   // null si hay varias sedes
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
        Integer remainingQuota,
        List<BranchOfferDto> branches  // vacío o 1+ sedes
) {}
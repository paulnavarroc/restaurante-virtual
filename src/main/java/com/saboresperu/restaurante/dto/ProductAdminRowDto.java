package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;

public record ProductAdminRowDto(
        Integer id,
        String name,
        String categoryName,
        String regionName,
        BigDecimal basePrice,
        boolean active
) {}
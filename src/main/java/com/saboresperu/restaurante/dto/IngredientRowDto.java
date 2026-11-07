package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;

public record IngredientRowDto(
        Integer id,
        String name,
        String unit,
        boolean unlimitedStock,
        BigDecimal minStockAlert,
        boolean active
) {}
package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;

public record RecipeItemRowDto(
        Integer id,
        Integer ingredientId,
        String ingredientName,
        String unit,
        BigDecimal quantity
) {}
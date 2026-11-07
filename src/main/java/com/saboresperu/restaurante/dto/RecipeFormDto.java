package com.saboresperu.restaurante.dto;

import java.util.List;

public record RecipeFormDto(
        Integer productId,
        String productName,
        Integer recipeId,
        Integer version,
        boolean active,
        List<RecipeItemFormDto> items
) {}
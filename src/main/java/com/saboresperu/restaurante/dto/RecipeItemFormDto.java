package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RecipeItemFormDto(
        @NotNull Integer ingredientId,
        @NotNull @DecimalMin("0.0001") BigDecimal quantity
) {}
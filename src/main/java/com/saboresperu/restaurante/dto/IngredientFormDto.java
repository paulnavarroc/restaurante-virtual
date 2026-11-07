package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record IngredientFormDto(
        Integer id,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 20) String unit,
        boolean unlimitedStock,
        BigDecimal minStockAlert,
        boolean active
) {}
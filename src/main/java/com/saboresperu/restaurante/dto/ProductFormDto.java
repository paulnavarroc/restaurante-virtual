package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductFormDto(
        Integer id,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String description,
        @NotNull Integer categoryId,
        Integer regionId,
        @NotNull @DecimalMin("0.00") BigDecimal basePrice,
        @Size(max = 500) String imageUrl,
        @Min(0) Integer prepTimeMinutes,
        boolean unlimitedStock,
        boolean active
) {}
package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BranchFormDto(
        Integer id,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 300) String address,
        @NotNull BigDecimal lat,
        @NotNull BigDecimal lng,
        @NotNull @DecimalMin("0.1") BigDecimal coverageRadiusKm,
        @Size(max = 30) String phone,
        @Size(max = 50) String tenantKey,
        boolean active
) {}
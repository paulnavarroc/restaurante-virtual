package com.saboresperu.restaurante.dto;

public record BranchAdminRowDto(
        Integer id,
        String name,
        String address,
        String phone,
        java.math.BigDecimal coverageRadiusKm,
        boolean active,
        String tenantKey
) {}
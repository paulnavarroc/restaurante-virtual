package com.saboresperu.restaurante.dto;

public record CustomerAddressAdminDto(
        Integer id,
        String alias,
        String line,
        String districtName,
        boolean primary,
        boolean active
) {}

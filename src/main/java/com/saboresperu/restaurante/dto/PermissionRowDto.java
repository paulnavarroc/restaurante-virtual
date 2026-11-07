package com.saboresperu.restaurante.dto;

public record PermissionRowDto(
        Integer id,
        String code,
        String module,
        String description,
        boolean active
) {}
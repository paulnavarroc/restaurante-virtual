package com.saboresperu.restaurante.dto;

public record PermissionGroupRowDto(
        Integer id,
        String code,
        String name,
        String description,
        boolean active,
        int permissionCount
) {}
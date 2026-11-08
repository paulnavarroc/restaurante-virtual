package com.saboresperu.restaurante.dto;

public record RoleAdminRowDto(
        Integer id,
        String name,
        String description,
        boolean active,
        long groupCount,
        long userCount
) {}
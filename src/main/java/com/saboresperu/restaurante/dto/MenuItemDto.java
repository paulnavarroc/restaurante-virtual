package com.saboresperu.restaurante.dto;

public record MenuItemDto(
        String code,
        String label,
        String route,
        String icon,
        String menuGroup,
        String context,
        int sortOrder
) {}
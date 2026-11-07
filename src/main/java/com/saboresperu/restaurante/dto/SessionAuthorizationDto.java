package com.saboresperu.restaurante.dto;

import java.util.List;
import java.util.Set;

public record SessionAuthorizationDto(
        long securityVersion,
        Set<String> permissionCodes,
        List<MenuItemDto> menuItems
) {}
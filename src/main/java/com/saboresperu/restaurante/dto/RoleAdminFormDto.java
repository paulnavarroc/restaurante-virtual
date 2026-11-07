package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RoleAdminFormDto(
        Integer id,
        @NotBlank @Size(max = 50) String name,
        @Size(max = 255) String description,
        boolean active,
        Set<Integer> permissionGroupIds,
        /** Códigos: "ALLOW:PERM_X" o "DENY:PERM_Y" */
        Set<String> permissionOverrides
) {}
package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record PermissionGroupFormDto(
        Integer id,
        @NotBlank @Size(max = 80) String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        boolean active,
        Set<Integer> permissionIds
) {}
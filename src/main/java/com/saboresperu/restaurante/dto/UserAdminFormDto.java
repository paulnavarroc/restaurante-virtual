package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserAdminFormDto(
        Integer id,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 30) String phone,
        /** Solo create o reset opcional */
        @Size(min = 6, max = 100) String password,
        boolean active,
        @NotEmpty Set<Integer> roleIds,
        /** CHEF / COURIER opcionales */
        Integer kitchenBranchId,
        Integer courierBranchId,
        /** INHERIT | RESTRICT | CUSTOM */
        String permissionMode,
        /** tokens "ALLOW:CODE" | "DENY:CODE" */
        Set<String> permissionOverrides
) {}
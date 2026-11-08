package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerProfileFormDto(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 30) String phone,
        @Size(max = 20) String documentType,
        @Size(max = 30) String documentNumber,
        /** Opcional: solo si quiere cambiar clave */
        @Size(min = 6, max = 100) String newPassword,
        String confirmPassword
) {}
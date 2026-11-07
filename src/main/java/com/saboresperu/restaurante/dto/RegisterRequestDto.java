package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100)
        String lastName,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo no válido")
        @Size(max = 150)
        String email,

        @Size(max = 30)
        String phone,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "Mínimo 6 caracteres")
        String password,

        @NotBlank(message = "Confirma la contraseña")
        String passwordConfirm
) {}
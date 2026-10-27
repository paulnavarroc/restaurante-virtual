package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UsuarioRegistroDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank
    private String apellido;

    @Email
    @NotBlank
    private String correo;

    @Size(min = 6, message = "Mínimo 6 caracteres")
    private String password;

    private String telefono;
}
package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistroRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank @Email
    private String correo;

    @NotBlank
    @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
    private String password;

    @NotBlank
    private String confirmPassword;

    private String telefono;

    private String documentoTipo;
    private String documentoNumero;

    // Dirección inicial
    private String direccionCompleta;
    private String distrito;
    private String provincia;
    private String departamento;
}
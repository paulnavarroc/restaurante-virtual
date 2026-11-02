package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo inválido")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
    private String password;

    @NotBlank(message = "Confirma la contraseña")
    private String confirmPassword;

    private String telefono;
    private String documentoTipo;
    private String documentoNumero;

    private String idDistrito;
    private String calle;
    private String numero;
    private String alias;
    private String urbanizacion;
    private String referencia;

    private String direccionCompleta;
    private String distrito;
    private String provincia;
    private String departamento;
}
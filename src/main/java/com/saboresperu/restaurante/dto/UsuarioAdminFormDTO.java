package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAdminFormDTO {
    private Integer id;
    @NotBlank
    private String nombre;
    @NotBlank private String apellido;
    @NotBlank @Email
    private String correo;
    private String telefono;
    /** Solo al crear o al resetear */
    private String password;
    @NotBlank private String rol; // CLIENTE, CHEF, ...
    private Boolean activo;
    /** Obligatorio si CHEF o REPARTIDOR */
    private Integer sedeId;
    private String cargo;     // cocina
    private String vehiculo;  // repartidor
}
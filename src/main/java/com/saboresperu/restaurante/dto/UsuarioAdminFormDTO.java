package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

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
    /** Obligatorio al crear; vacío al editar = no cambiar */
    private String password;

    @NotBlank private String rol; // CLIENTE, CHEF, REPARTIDOR, ADMINISTRADOR, OPERACIONES
    private Boolean activo = true;

    private Integer sedeId;   // CHEF / REPARTIDOR
    private String cargo;     // CHEF
    private String vehiculo;  // REPARTIDOR
}
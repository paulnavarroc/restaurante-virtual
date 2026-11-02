package com.saboresperu.restaurante.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UsuarioAdminListDTO {
    private Integer id;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private Boolean activo;
    private List<String> roles;
    private String sedeNombre;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoAcceso;
    private String ultimaIp;
}
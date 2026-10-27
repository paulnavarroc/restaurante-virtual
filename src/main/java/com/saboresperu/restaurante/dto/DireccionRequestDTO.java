package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DireccionRequestDTO {

    private String alias;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccionCompleta;

    private String referencia;

    @NotBlank(message = "El distrito es obligatorio")
    private String distrito;

    private String provincia = "Lima";
    private String departamento = "Lima";

    private Boolean esPrincipal = false;
}
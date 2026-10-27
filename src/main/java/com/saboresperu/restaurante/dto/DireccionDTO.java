package com.saboresperu.restaurante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DireccionDTO {
    private Integer id;
    private String alias;
    private String direccionCompleta;
    private String referencia;
    private String distrito;
    private String provincia;
    private String departamento;
    private Boolean esPrincipal;
}
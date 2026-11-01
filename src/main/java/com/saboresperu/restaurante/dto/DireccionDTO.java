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
    private String calle;
    private String numero;
    private String distrito;
    private Boolean esPrincipal;
    private String resumen;
}
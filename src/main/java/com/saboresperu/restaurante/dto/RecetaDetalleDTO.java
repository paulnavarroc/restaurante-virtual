package com.saboresperu.restaurante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecetaDetalleDTO {
    private Integer insumoId;
    private String insumoNombre;
    private String unidad;
    private BigDecimal cantidad;
}
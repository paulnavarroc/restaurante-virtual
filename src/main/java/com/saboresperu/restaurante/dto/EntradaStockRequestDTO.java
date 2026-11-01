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
public class EntradaStockRequestDTO {
    private Integer sedeId;
    private Integer insumoId;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private String observacion;
}
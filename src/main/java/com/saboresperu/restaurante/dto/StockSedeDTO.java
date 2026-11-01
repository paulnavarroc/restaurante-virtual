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
public class StockSedeDTO {
    private Integer stockId;
    private Integer sedeId;
    private String sedeNombre;
    private Integer insumoId;
    private String insumoNombre;
    private String unidad;
    private BigDecimal cantidadDisponible;
    private BigDecimal cantidadReservada;
    private BigDecimal disponibleNeto;
    private BigDecimal ultimoCosto;
    private boolean alertaMinimo;
}
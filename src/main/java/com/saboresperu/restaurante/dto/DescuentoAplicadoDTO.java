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
public class DescuentoAplicadoDTO {
    private String tipo; // PRODUCTO, CUPON, DELIVERY
    private Integer promocionId;
    private Integer cuponId;
    private BigDecimal monto;
    private String descripcion;
}
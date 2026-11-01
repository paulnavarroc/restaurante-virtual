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
public class DetallePedidoResponseDTO {
    private Integer productoId;
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal precioLista;
    private BigDecimal precioUnitarioFinal;
    private BigDecimal descuentoUnitario;
    private BigDecimal subtotal;
}
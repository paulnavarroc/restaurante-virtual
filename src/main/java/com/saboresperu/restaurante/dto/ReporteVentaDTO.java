package com.saboresperu.restaurante.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ReporteVentaDTO {
    private Integer pedidoId;
    private LocalDateTime fecha;
    private String estado;
    private BigDecimal subtotal;
    private BigDecimal delivery;
    private BigDecimal descuento;
    private BigDecimal total;
    private String direccion;
}
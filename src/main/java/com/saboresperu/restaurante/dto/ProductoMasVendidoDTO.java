package com.saboresperu.restaurante.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProductoMasVendidoDTO {
    private Integer productoId;
    private String nombre;
    private String region;
    private Long cantidadVendida;
    private BigDecimal montoTotal;
}
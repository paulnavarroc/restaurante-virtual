package com.saboresperu.restaurante.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CarritoItemDTO {
    private Integer productoId;
    private String nombre;
    private String imagenUrl;
    private BigDecimal precio;
    private Integer cantidad;
    private String region;

    public BigDecimal getSubtotal() {
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }
}
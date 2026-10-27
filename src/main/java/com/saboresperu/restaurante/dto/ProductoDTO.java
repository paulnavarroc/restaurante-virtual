// ProductoDTO.java
package com.saboresperu.restaurante.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProductoDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String imagenUrl;
    private Integer tiempoPreparacion;
    private Boolean disponible;
    private String region;
    private String categoria;
}
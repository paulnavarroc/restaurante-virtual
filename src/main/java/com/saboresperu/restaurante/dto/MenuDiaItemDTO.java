package com.saboresperu.restaurante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDiaItemDTO {
    private Integer id;
    private Integer productoId;
    private String nombre;
    private String descripcion;
    private String imagenUrl;
    private BigDecimal precioLista;
    private BigDecimal precioFinal; // por ahora = lista; promos después
    private Boolean disponible;
    private Boolean agotado;
    private String categoria;
    private String region;
    private List<String> turnos;
}

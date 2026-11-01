package com.saboresperu.restaurante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecetaDTO {
    private Integer id;
    private Integer productoId;
    private String productoNombre;
    private Integer version;
    private Boolean activa;
    private List<RecetaDetalleDTO> detalles;
}
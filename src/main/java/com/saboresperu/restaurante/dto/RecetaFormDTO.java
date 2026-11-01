package com.saboresperu.restaurante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecetaFormDTO {
    private Integer productoId;
    private String productoNombre;
    private Integer version;
    private Boolean activa;
    @Builder.Default
    private List<RecetaDetalleFormDTO> detalles = new ArrayList<>();
}

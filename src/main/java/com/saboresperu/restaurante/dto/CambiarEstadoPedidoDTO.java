package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CambiarEstadoPedidoDTO {
    @NotBlank
    private String nuevoEstado;     // EN_PREPARACION, LISTO, EN_CAMINO, ENTREGADO, etc.
    private String observacion;
}
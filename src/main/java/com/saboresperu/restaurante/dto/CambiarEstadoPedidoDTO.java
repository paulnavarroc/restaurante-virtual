package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoPedidoDTO {
    @NotNull
    private String nuevoEstado;
    private String observacion;
}
package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DetallePedidoRequestDTO {
    @NotNull
    private Integer productoId;

    @NotNull @Min(1)
    private Integer cantidad;
}
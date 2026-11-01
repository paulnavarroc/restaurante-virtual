package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoRequestDTO {
    @NotNull
    private Integer productoId;
    private Integer menuDiaItemId;
    @NotNull @Min(1)
    private Integer cantidad;
}
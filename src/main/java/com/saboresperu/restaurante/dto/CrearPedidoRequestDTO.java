package com.saboresperu.restaurante.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CrearPedidoRequestDTO {

    @NotNull
    private Integer direccionId;

    @NotNull
    private Integer metodoPagoId;

    private String observaciones;

    @NotEmpty
    @Valid
    private List<DetallePedidoRequestDTO> items;
}
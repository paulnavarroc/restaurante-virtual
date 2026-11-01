package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class CrearPedidoRequestDTO {
    @NotNull
    private Integer direccionId;
    @NotNull
    private Integer metodoPagoId;
    private String observaciones;
    private String codigoCupon;
    @NotEmpty
    private List<DetallePedidoRequestDTO> items = new ArrayList<>();
}
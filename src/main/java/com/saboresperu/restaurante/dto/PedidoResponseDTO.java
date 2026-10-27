package com.saboresperu.restaurante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {
    private Integer id;
    private String estado;
    private LocalDateTime fechaPedido;
    private BigDecimal subtotal;
    private BigDecimal costoDelivery;
    private BigDecimal descuento;
    private BigDecimal total;
    private String observaciones;
    private String direccion;

    @Builder.Default
    private List<DetallePedidoResponseDTO> items = new ArrayList<>();
}
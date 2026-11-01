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
    private Integer sedeId;
    private String sedeNombre;
    private BigDecimal subtotal;
    private BigDecimal descuentoTotal;
    private BigDecimal costoDelivery;
    private BigDecimal total;
    private BigDecimal distanciaKm;
    private String direccion;
    private String observaciones;
    private String codigoCupon;
    @Builder.Default
    private List<DetallePedidoResponseDTO> items = new ArrayList<>();
}
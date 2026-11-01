package com.saboresperu.restaurante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private long pedidosHoy;
    private long pedidosPendientesCocina;
    private long pedidosEnCamino;
    private BigDecimal ventasHoy;
    private long clientesActivos; // opcional: con pedido histórico
    private long alertasStock;
    private Integer sedeIdFiltro;
    private String sedeNombre;

    @Builder.Default
    private List<PedidoResponseDTO> ultimosPedidos = new ArrayList<>();

    @Builder.Default
    private List<StockSedeDTO> stockBajo = new ArrayList<>();

    @Builder.Default
    private List<EstadoCountDTO> pedidosPorEstado = new ArrayList<>();
}
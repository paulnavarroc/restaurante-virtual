package com.saboresperu.restaurante.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntregaDTO {
    private Integer id;
    private Integer pedidoId;
    private String estadoPedido;
    private String estadoEntrega;
    private Integer repartidorId;
    private String repartidorNombre;
    private String direccion;
    private String clienteNombre;
    private BigDecimal total;
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaAsignacion;
    private List<DetallePedidoResponseDTO> items;
}
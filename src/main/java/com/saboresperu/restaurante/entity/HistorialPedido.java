package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "HistorialPedido")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class HistorialPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdHistorial")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdEstadoPedido", nullable = false)
    private EstadoPedido estadoPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUsuario")
    private Usuario usuario;

    @Column(name = "Observacion", length = 500)
    private String observacion;

    @Column(name = "FechaCambio", nullable = false)
    private LocalDateTime fechaCambio;
}
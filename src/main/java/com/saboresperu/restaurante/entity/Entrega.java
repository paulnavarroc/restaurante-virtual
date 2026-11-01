package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Entregas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdEntrega")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdPedido", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdRepartidor")
    private Repartidor repartidor;

    /**
     * PENDIENTE_ASIGNACION, ASIGNADA, EN_CAMINO, ENTREGADA, FALLIDA
     */
    @Column(name = "Estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "FechaAsignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "FechaRecojo")
    private LocalDateTime fechaRecojo;

    @Column(name = "FechaEntrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "Observacion", length = 400)
    private String observacion;
}
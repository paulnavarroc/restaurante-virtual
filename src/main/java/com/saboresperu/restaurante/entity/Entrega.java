package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Entregas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdEntrega")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPedido", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdRepartidor")
    private Repartidor repartidor;

    @Column(name = "Estado", nullable = false, length = 40)
    private String estado;          // PENDIENTE, ASIGNADA, RECOGIDA, EN_CAMINO, ENTREGADA, CANCELADA

    @Column(name = "FechaAsignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "FechaRecogida")
    private LocalDateTime fechaRecogida;

    @Column(name = "FechaEntrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "LatitudEntrega", precision = 10, scale = 7)
    private BigDecimal latitudEntrega;

    @Column(name = "LongitudEntrega", precision = 10, scale = 7)
    private BigDecimal longitudEntrega;

    @Column(name = "Observacion", length = 500)
    private String observacion;
}
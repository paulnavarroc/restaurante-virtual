package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CuponUsos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuponUso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdCuponUso")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdCupon", nullable = false)
    private Cupon cupon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdCliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdPedido", nullable = false)
    private Pedido pedido;

    @Column(name = "Fecha", nullable = false)
    private LocalDateTime fecha;
}
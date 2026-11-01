package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "TarifasDelivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifaDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdTarifa")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdSede", nullable = false)
    private Sede sede;

    @Column(name = "KmDesde", nullable = false, precision = 8, scale = 2)
    private BigDecimal kmDesde;

    @Column(name = "KmHasta", nullable = false, precision = 8, scale = 2)
    private BigDecimal kmHasta;

    @Column(name = "Costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;
}
package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "StockInsumos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdStockInsumo")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdSede", nullable = false)
    private Sede sede;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdInsumo", nullable = false)
    private Insumo insumo;

    @Column(name = "CantidadDisponible", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadDisponible;

    @Column(name = "CantidadReservada", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadReservada;

    @Column(name = "CostoPromedio", precision = 12, scale = 4)
    private BigDecimal costoPromedio;

    @Column(name = "UltimoCosto", precision = 12, scale = 4)
    private BigDecimal ultimoCosto;

    @Column(name = "StockMinimoAlerta", precision = 12, scale = 4)
    private BigDecimal stockMinimoAlerta;

    @Version
    @Column(name = "Version", nullable = false)
    private Integer version;
}
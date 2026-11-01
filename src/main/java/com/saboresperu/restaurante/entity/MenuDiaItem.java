package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "MenuDiaItems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuDiaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdMenuDiaItem")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdMenuDia", nullable = false)
    private MenuDia menuDia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdProducto", nullable = false)
    private Producto producto;

    @Column(name = "Disponible", nullable = false)
    private Boolean disponible;

    @Column(name = "PrecioDia", precision = 10, scale = 2)
    private BigDecimal precioDia;

    @Column(name = "CupoMaximo")
    private Integer cupoMaximo;

    @Column(name = "CantidadVendida", nullable = false)
    private Integer cantidadVendida;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "MenuDiaItemTurnos",
            joinColumns = @JoinColumn(name = "IdMenuDiaItem"),
            inverseJoinColumns = @JoinColumn(name = "IdSedeTurno")
    )
    @Builder.Default
    private Set<SedeTurno> turnos = new HashSet<>();
}
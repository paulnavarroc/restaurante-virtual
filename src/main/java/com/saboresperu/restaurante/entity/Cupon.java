package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Cupones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdCupon")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdPromocion", nullable = false)
    private Promocion promocion;

    @Column(name = "Codigo", nullable = false, length = 40, unique = true)
    private String codigo;

    @Column(name = "UsoMaxGlobal")
    private Integer usoMaxGlobal;

    @Column(name = "UsoMaxPorCliente")
    private Integer usoMaxPorCliente;

    @Column(name = "UsosActuales", nullable = false)
    private Integer usosActuales;

    @Column(name = "Activo", nullable = false)
    private Boolean activo;
}
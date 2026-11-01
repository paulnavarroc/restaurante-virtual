package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "UbigeoDistritos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbigeoDistrito {

    @Id
    @Column(name = "IdDistrito", length = 6)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdProvincia", nullable = false)
    private UbigeoProvincia provincia;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "LatCentro", precision = 10, scale = 7)
    private BigDecimal latCentro;

    @Column(name = "LngCentro", precision = 10, scale = 7)
    private BigDecimal lngCentro;
}
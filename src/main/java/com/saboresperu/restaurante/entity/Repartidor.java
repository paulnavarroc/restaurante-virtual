package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Repartidores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repartidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdRepartidor")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdUsuario", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdSede", nullable = false)
    private Sede sede;

    @Column(name = "Telefono", length = 30)
    private String telefono;

    @Column(name = "Vehiculo", length = 50)
    private String vehiculo;

    /** OFFLINE, DISPONIBLE, EN_ENTREGA */
    @Column(name = "Estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "Activo", nullable = false)
    private Boolean activo;

    @Column(name = "UltimaLat", precision = 10, scale = 7)
    private BigDecimal ultimaLat;

    @Column(name = "UltimaLng", precision = 10, scale = 7)
    private BigDecimal ultimaLng;
}
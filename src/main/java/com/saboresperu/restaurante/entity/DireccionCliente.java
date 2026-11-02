package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "DireccionesCliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DireccionCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdDireccion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdCliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdDistrito", nullable = false, columnDefinition = "char(6)")
    private UbigeoDistrito distrito;

    @Column(name = "Alias", length = 50)
    private String alias;

    @Column(name = "Urbanizacion", length = 120)
    private String urbanizacion;

    @Column(name = "Calle", nullable = false, length = 200)
    private String calle;

    @Column(name = "Numero", length = 30)
    private String numero;

    @Column(name = "Referencia", length = 300)
    private String referencia;

    @Column(name = "Lat", precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(name = "Lng", precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "EsPrincipal", nullable = false)
    private Boolean esPrincipal;

    @Column(name = "Activa", nullable = false)
    private Boolean activa;

    @Column(name = "FechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
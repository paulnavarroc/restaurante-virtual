package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Promociones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdPromocion")
    private Integer id;

    @Column(name = "Nombre", nullable = false, length = 150)
    private String nombre;

    /**
     * PORCENTAJE_PRODUCTO, MONTO_PRODUCTO, NX_M,
     * CUPON, HAPPY_HOUR, DELIVERY_GRATIS
     */
    @Column(name = "Tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "Valor", precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "LlevaN")
    private Integer llevaN;

    @Column(name = "PagaM")
    private Integer pagaM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSede")
    private Sede sede;

    @Column(name = "FechaHoraInicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "FechaHoraFin", nullable = false)
    private LocalDateTime fechaHoraFin;

    @Column(name = "CupoGlobal")
    private Integer cupoGlobal;

    @Column(name = "UsosActuales", nullable = false)
    private Integer usosActuales;

    @Column(name = "Combinable", nullable = false)
    private Boolean combinable;

    @Column(name = "RequiereCodigo", nullable = false)
    private Boolean requiereCodigo;

    @Column(name = "Prioridad", nullable = false)
    private Integer prioridad;

    @Column(name = "Activa", nullable = false)
    private Boolean activa;
}
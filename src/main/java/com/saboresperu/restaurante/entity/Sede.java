package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Sedes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdSede")
    private Integer id;

    @Column(name = "Nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "Direccion", nullable = false, length = 300)
    private String direccion;

    @Column(name = "Lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(name = "Lng", nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "RadioCoberturaKm", nullable = false, precision = 8, scale = 2)
    private BigDecimal radioCoberturaKm;

    @Column(name = "Activa", nullable = false)
    private Boolean activa;

    @Column(name = "TenantKey", length = 50)
    private String tenantKey;

    @Column(name = "Telefono", length = 30)
    private String telefono;

    @Column(name = "FechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
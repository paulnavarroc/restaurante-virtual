package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Productos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdProducto")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdRegion", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdCategoria", nullable = false)
    private Categoria categoria;

    @Column(name = "Nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "Descripcion", length = 500)
    private String descripcion;

    @Column(name = "Precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "ImagenUrl", length = 500)
    private String imagenUrl;

    @Column(name = "TiempoPreparacion")
    private Integer tiempoPreparacion;

    @Column(name = "Disponible", nullable = false)
    private Boolean disponible = true;

    @Column(name = "Activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "FechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro;
}
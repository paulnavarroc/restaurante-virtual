package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdProducto")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdCategoria", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdRegion")
    private Region region;

    @Column(name = "Nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "Descripcion", length = 1000)
    private String descripcion;

    @Column(name = "PrecioBase", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "ImagenUrl", length = 500)
    private String imagenUrl;

    @Column(name = "TiempoPreparacionMin")
    private Integer tiempoPreparacionMin;

    @Column(name = "StockIlimitado", nullable = false)
    private Boolean stockIlimitado;

    @Column(name = "Activo", nullable = false)
    private Boolean activo;

    @Column(name = "FechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro;
}
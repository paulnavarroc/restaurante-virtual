package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Insumos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdInsumo")
    private Integer id;

    @Column(name = "Nombre", nullable = false, length = 150, unique = true)
    private String nombre;

    @Column(name = "Unidad", nullable = false, length = 20)
    private String unidad;

    @Column(name = "StockIlimitado", nullable = false)
    private Boolean stockIlimitado;

    @Column(name = "StockMinimoAlerta", precision = 12, scale = 4)
    private BigDecimal stockMinimoAlerta;

    @Column(name = "Activo", nullable = false)
    private Boolean activo;
}
package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Regiones")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdRegion")
    private Integer id;

    @Column(name = "Nombre", nullable = false, unique = true, length = 50)
    private String nombre;          // COSTA, SIERRA, SELVA

    @Column(name = "Descripcion", length = 250)
    private String descripcion;

    @Column(name = "Activo", nullable = false)
    private Boolean activo = true;
}
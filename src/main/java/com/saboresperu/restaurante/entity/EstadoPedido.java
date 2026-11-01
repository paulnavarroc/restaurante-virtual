package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "EstadosPedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdEstadoPedido")
    private Integer id;

    @Column(name = "Nombre", nullable = false, length = 30, unique = true)
    private String nombre;

    @Column(name = "Descripcion", length = 200)
    private String descripcion;
}
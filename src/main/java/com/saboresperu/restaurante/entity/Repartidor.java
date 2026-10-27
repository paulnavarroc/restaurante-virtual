package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Repartidores")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Repartidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdRepartidor")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUsuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "Documento", length = 30)
    private String documento;

    @Column(name = "TipoVehiculo", length = 50)
    private String tipoVehiculo;

    @Column(name = "Placa", length = 20)
    private String placa;

    @Column(name = "Disponible", nullable = false)
    private Boolean disponible = true;

    @Column(name = "Activo", nullable = false)
    private Boolean activo = true;
}
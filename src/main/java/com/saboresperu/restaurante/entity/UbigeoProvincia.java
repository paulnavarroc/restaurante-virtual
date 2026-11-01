package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "UbigeoProvincias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbigeoProvincia {

    @Id
    @Column(name = "IdProvincia", length = 4)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdDepartamento", nullable = false)
    private UbigeoDepartamento departamento;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;
}
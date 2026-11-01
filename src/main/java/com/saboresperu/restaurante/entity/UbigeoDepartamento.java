package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "UbigeoDepartamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbigeoDepartamento {

    @Id
    @Column(name = "IdDepartamento", length = 2)
    private String id;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;
}
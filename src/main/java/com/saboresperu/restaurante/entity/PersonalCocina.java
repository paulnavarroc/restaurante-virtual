package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PersonalCocina")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalCocina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdPersonalCocina")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdUsuario", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdSede", nullable = false)
    private Sede sede;

    @Column(name = "Cargo", length = 50)
    private String cargo;

    @Column(name = "Activo", nullable = false)
    private Boolean activo;

    @Column(name = "FechaAsignacion", nullable = false)
    private LocalDateTime fechaAsignacion;
}
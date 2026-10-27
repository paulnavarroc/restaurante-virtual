package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Clientes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdCliente")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUsuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "FechaNacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "DocumentoTipo", length = 20)
    private String documentoTipo;

    @Column(name = "DocumentoNumero", length = 30)
    private String documentoNumero;

    @Column(name = "FechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro;
}
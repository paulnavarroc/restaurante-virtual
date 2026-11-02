package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "UbigeoDepartamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbigeoDepartamento {

    @Id
    @Column(name = "IdDepartamento", length = 2, columnDefinition = "char(2)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String id;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;
}
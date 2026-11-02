package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "UbigeoProvincias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbigeoProvincia {

    @Id
    @Column(name = "IdProvincia", length = 4, columnDefinition = "char(4)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdDepartamento", nullable = false, columnDefinition = "char(2)")
    private UbigeoDepartamento departamento;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;
}
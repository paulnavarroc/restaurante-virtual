package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Direcciones")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdDireccion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdCliente", nullable = false)
    private Cliente cliente;

    @Column(name = "Alias", length = 50)
    private String alias;

    @Column(name = "DireccionCompleta", nullable = false, length = 300)
    private String direccionCompleta;

    @Column(name = "Referencia", length = 300)
    private String referencia;

    @Column(name = "Distrito", nullable = false, length = 100)
    private String distrito;

    @Column(name = "Provincia", nullable = false, length = 100)
    private String provincia;

    @Column(name = "Departamento", nullable = false, length = 100)
    private String departamento;

    @Column(name = "CodigoPostal", length = 20)
    private String codigoPostal;

    @Column(name = "Latitud", precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "Longitud", precision = 10, scale = 7)
    private BigDecimal longitud;

    @Column(name = "EsPrincipal", nullable = false)
    private Boolean esPrincipal = false;

    @Column(name = "Activo", nullable = false)
    private Boolean activo = true;
}
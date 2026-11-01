package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ConfiguracionSede")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionSede {

    @Id
    @Column(name = "IdSede")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "IdSede")
    private Sede sede;

    @Column(name = "DeliveryGratisDesdeMonto", precision = 10, scale = 2)
    private BigDecimal deliveryGratisDesdeMonto;

    @Column(name = "DeliveryGratisHastaKm", precision = 8, scale = 2)
    private BigDecimal deliveryGratisHastaKm;

    @Column(name = "CostoDeliveryMinimo", precision = 10, scale = 2)
    private BigDecimal costoDeliveryMinimo;

    @Column(name = "PedidoMinimo", precision = 10, scale = 2)
    private BigDecimal pedidoMinimo;
}
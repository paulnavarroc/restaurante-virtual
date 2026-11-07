package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "GeoDistricts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeoDistrict {
    @Id
    @Column(name = "IdDistrict", length = 6, columnDefinition = "char(6)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdProvince", nullable = false, columnDefinition = "char(4)")
    private GeoProvince province;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "CenterLat", precision = 10, scale = 7)
    private BigDecimal centerLat;

    @Column(name = "CenterLng", precision = 10, scale = 7)
    private BigDecimal centerLng;
}

package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "DeliveryTariffs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryTariff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdDeliveryTariff")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdBranch", nullable = false)
    private Branch branch;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "RuleType", nullable = false, length = 20)
    private String ruleType;

    @Column(name = "MinDistanceKm", precision = 8, scale = 2)
    private BigDecimal minDistanceKm;

    @Column(name = "MaxDistanceKm", precision = 8, scale = 2)
    private BigDecimal maxDistanceKm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdDistrict", columnDefinition = "char(6)")
    private GeoDistrict district;

    @Column(name = "Fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;

    @Column(name = "SortOrder", nullable = false)
    private Integer sortOrder;
}

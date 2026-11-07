package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Branches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdBranch")
    private Integer id;

    @Column(name = "Name", nullable = false, length = 120)
    private String name;

    @Column(name = "Address", nullable = false, length = 300)
    private String address;

    @Column(name = "Lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(name = "Lng", nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "CoverageRadiusKm", nullable = false, precision = 8, scale = 2)
    private BigDecimal coverageRadiusKm;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;

    @Column(name = "TenantKey", length = 50)
    private String tenantKey;

    @Column(name = "Phone", length = 30)
    private String phone;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
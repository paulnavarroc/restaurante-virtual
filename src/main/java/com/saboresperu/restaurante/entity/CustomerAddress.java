package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CustomerAddresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdAddress")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdCustomer", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdDistrict", nullable = false, columnDefinition = "char(6)")
    private GeoDistrict district;

    @Column(name = "Alias", length = 50)
    private String alias;

    @Column(name = "Urbanization", length = 120)
    private String urbanization;

    @Column(name = "Street", nullable = false, length = 200)
    private String street;

    @Column(name = "Number", length = 30)
    private String number;

    @Column(name = "Reference", length = 300)
    private String reference;

    @Column(name = "Lat", precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(name = "Lng", precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "IsPrimary", nullable = false)
    private Boolean isPrimary;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
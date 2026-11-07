package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Couriers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdCourier")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdUser", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdBranch", nullable = false)
    private Branch branch;

    @Column(name = "Phone", length = 30)
    private String phone;

    @Column(name = "Vehicle", length = 50)
    private String vehicle;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;

    @Column(name = "LastLat", precision = 10, scale = 7)
    private BigDecimal lastLat;

    @Column(name = "LastLng", precision = 10, scale = 7)
    private BigDecimal lastLng;
}

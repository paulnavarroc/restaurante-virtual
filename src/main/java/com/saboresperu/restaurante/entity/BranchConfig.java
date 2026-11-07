package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "BranchConfigs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchConfig {
    @Id
    @Column(name = "IdBranch")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "IdBranch")
    private Branch branch;

    @Column(name = "MinOrderAmount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "DefaultDeliveryFee", precision = 10, scale = 2)
    private BigDecimal defaultDeliveryFee;

    @Column(name = "FreeDeliveryFromAmount", precision = 10, scale = 2)
    private BigDecimal freeDeliveryFromAmount;

    @Column(name = "AvgPrepTimeMinutes")
    private Integer avgPrepTimeMinutes;

    @Column(name = "AcceptsOnlineOrders", nullable = false)
    private Boolean acceptsOnlineOrders;

    @Column(name = "OpenTime")
    private LocalTime openTime;

    @Column(name = "CloseTime")
    private LocalTime closeTime;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;
}
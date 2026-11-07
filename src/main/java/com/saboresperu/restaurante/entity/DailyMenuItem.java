package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "DailyMenuItems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdDailyMenuItem")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdDailyMenu", nullable = false)
    private DailyMenu dailyMenu;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdProduct", nullable = false)
    private Product product;

    @Column(name = "IsAvailable", nullable = false)
    private Boolean isAvailable;

    @Column(name = "PriceOverride", precision = 10, scale = 2)
    private BigDecimal priceOverride;

    @Column(name = "MaxQuota")
    private Integer maxQuota;

    @Column(name = "SoldQuantity", nullable = false)
    private Integer soldQuantity;

    @ManyToMany
    @JoinTable(
            name = "DailyMenuItemShifts",
            joinColumns = @JoinColumn(name = "IdDailyMenuItem"),
            inverseJoinColumns = @JoinColumn(name = "IdBranchShift")
    )
    @Builder.Default
    private Set<BranchShift> shifts = new HashSet<>();
}
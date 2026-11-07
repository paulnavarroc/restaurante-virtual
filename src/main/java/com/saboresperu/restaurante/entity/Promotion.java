package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdPromotion")
    private Integer id;

    @Column(name = "Code", length = 40)
    private String code;

    @Column(name = "Name", nullable = false, length = 150)
    private String name;

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "PromoType", nullable = false, length = 30)
    private String promoType;

    @Column(name = "DiscountPercent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "DiscountAmount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "Scope", nullable = false, length = 20)
    private String scope;

    @Column(name = "StartsAt", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "EndsAt", nullable = false)
    private LocalDateTime endsAt;

    @Column(name = "Priority", nullable = false)
    private Integer priority;

    @Column(name = "IsStackable", nullable = false)
    private Boolean isStackable;

    @Column(name = "IsAutomatic", nullable = false)
    private Boolean isAutomatic;

    @Column(name = "MinOrderAmount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "MaxDiscountAmount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdBranch")
    private Branch branch;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdCreatedByUser")
    private User createdBy;

    @OneToMany(mappedBy = "promotion")
    @Builder.Default
    private List<PromotionProduct> promotionProducts = new ArrayList<>();

    @OneToMany(mappedBy = "promotion")
    @Builder.Default
    private List<PromotionCategory> promotionCategories = new ArrayList<>();
}
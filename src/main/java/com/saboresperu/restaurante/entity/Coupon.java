package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdCoupon")
    private Integer id;

    @Column(name = "Code", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "Description", length = 300)
    private String description;

    @Column(name = "DiscountType", nullable = false, length = 20)
    private String discountType;

    @Column(name = "DiscountValue", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "MinOrderAmount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "MaxUses")
    private Integer maxUses;

    @Column(name = "UsedCount", nullable = false)
    private Integer usedCount;

    @Column(name = "StartsAt", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "EndsAt", nullable = false)
    private LocalDateTime endsAt;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;
}

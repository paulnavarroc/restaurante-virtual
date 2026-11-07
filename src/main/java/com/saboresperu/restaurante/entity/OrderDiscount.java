package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "OrderDiscounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDiscount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdOrderDiscount")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdOrder", nullable = false)
    private Order order;

    @Column(name = "SourceType", nullable = false, length = 20)
    private String sourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPromotion")
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdCoupon")
    private Coupon coupon;

    @Column(name = "CodeSnapshot", length = 40)
    private String codeSnapshot;

    @Column(name = "NameSnapshot", nullable = false, length = 150)
    private String nameSnapshot;

    @Column(name = "DiscountAmount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;
}

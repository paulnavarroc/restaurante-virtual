package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PromotionProducts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PromotionProductId.class)
public class PromotionProduct {

    @Id
    @Column(name = "IdPromotion")
    private Integer promotionId;

    @Id
    @Column(name = "IdProduct")
    private Integer productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPromotion", insertable = false, updatable = false)
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdProduct", insertable = false, updatable = false)
    private Product product;
}
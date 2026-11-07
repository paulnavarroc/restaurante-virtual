package com.saboresperu.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PromotionCategories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PromotionCategoryId.class)
public class PromotionCategory {

    @Id
    @Column(name = "IdPromotion")
    private Integer promotionId;

    @Id
    @Column(name = "IdCategory")
    private Integer categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPromotion", insertable = false, updatable = false)
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdCategory", insertable = false, updatable = false)
    private Category category;
}
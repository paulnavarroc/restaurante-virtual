package com.saboresperu.restaurante.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionCategoryId implements Serializable {
    private Integer promotionId;
    private Integer categoryId;
}
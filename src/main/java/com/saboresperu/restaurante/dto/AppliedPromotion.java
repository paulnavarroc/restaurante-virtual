package com.saboresperu.restaurante.dto;

import com.saboresperu.restaurante.entity.Promotion;

import java.math.BigDecimal;

public record AppliedPromotion(
        Promotion promotion,
        BigDecimal discountAmount
) {}
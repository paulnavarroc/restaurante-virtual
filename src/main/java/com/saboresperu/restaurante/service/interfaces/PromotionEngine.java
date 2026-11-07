package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.AppliedPromotion;
import com.saboresperu.restaurante.dto.CartDto;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionEngine {
    /**
     * Calcula descuentos automáticos (no cupón).
     * @param deliveryFee fee ya calculado (para scope DELIVERY)
     */
    List<AppliedPromotion> evaluateAutomatic(CartDto cart, Integer branchId, BigDecimal subtotal, BigDecimal deliveryFee);
}
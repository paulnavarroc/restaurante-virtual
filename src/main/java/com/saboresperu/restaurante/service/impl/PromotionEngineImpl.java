package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.PromoScopes;
import com.saboresperu.restaurante.domain.PromoTypes;
import com.saboresperu.restaurante.dto.AppliedPromotion;
import com.saboresperu.restaurante.dto.CartDto;
import com.saboresperu.restaurante.dto.CartItemDto;
import com.saboresperu.restaurante.entity.Product;
import com.saboresperu.restaurante.entity.Promotion;
import com.saboresperu.restaurante.repository.ProductRepository;
import com.saboresperu.restaurante.repository.PromotionCategoryRepository;
import com.saboresperu.restaurante.repository.PromotionProductRepository;
import com.saboresperu.restaurante.repository.PromotionRepository;
import com.saboresperu.restaurante.service.interfaces.PromotionEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PromotionEngineImpl implements PromotionEngine {

    private final PromotionRepository promotionRepository;
    private final PromotionProductRepository promotionProductRepository;
    private final PromotionCategoryRepository promotionCategoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AppliedPromotion> evaluateAutomatic(CartDto cart, Integer branchId,
                                                    BigDecimal subtotal,
                                                    BigDecimal deliveryFee) {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promos = promotionRepository.findActiveAutomatic(now, branchId);
        List<AppliedPromotion> applied = new ArrayList<>();
        BigDecimal remainingSubtotal = money(subtotal);
        boolean usedNonStackable = false;

        for (Promotion promo : promos) {
            if (usedNonStackable && !Boolean.TRUE.equals(promo.getIsStackable())) {
                continue;
            }
            if (promo.getMinOrderAmount() != null
                    && subtotal.compareTo(promo.getMinOrderAmount()) < 0) {
                continue;
            }

            BigDecimal discount = calculate(promo, cart, remainingSubtotal, deliveryFee);
            if (discount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (promo.getMaxDiscountAmount() != null
                    && discount.compareTo(promo.getMaxDiscountAmount()) > 0) {
                discount = money(promo.getMaxDiscountAmount());
            }

            applied.add(new AppliedPromotion(promo, discount));

            if (PromoScopes.ORDER.equals(promo.getScope())
                    || PromoScopes.PRODUCT.equals(promo.getScope())
                    || PromoScopes.CATEGORY.equals(promo.getScope())) {
                remainingSubtotal = remainingSubtotal.subtract(discount).max(BigDecimal.ZERO);
            }

            if (!Boolean.TRUE.equals(promo.getIsStackable())) {
                usedNonStackable = true;
            }
        }
        return applied;
    }

    private BigDecimal calculate(Promotion promo, CartDto cart,
                                 BigDecimal subtotal, BigDecimal deliveryFee) {
        return switch (promo.getScope()) {
            case PromoScopes.ORDER -> amountFromBase(promo, subtotal);
            case PromoScopes.DELIVERY -> amountFromBase(promo, deliveryFee);
            case PromoScopes.PRODUCT -> amountFromEligibleLines(promo, cart, true);
            case PromoScopes.CATEGORY -> amountFromEligibleLines(promo, cart, false);
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal amountFromBase(Promotion promo, BigDecimal base) {
        if (base == null || base.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (PromoTypes.PERCENT.equals(promo.getPromoType())) {
            return money(base.multiply(promo.getDiscountPercent())
                    .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        }
        return money(promo.getDiscountAmount()).min(money(base));
    }

    private BigDecimal amountFromEligibleLines(Promotion promo, CartDto cart, boolean byProduct) {
        Set<Integer> targetIds = byProduct
                ? new HashSet<>(promotionProductRepository.findByPromotionId(promo.getId())
                .stream().map(pp -> pp.getProductId()).toList())
                : new HashSet<>(promotionCategoryRepository.findByPromotionId(promo.getId())
                .stream().map(pc -> pc.getCategoryId()).toList());

        if (targetIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal eligible = BigDecimal.ZERO;
        for (CartItemDto line : cart.getItems()) {
            Product p = productRepository.findById(line.getProductId()).orElse(null);
            if (p == null) continue;
            boolean match = byProduct
                    ? targetIds.contains(p.getId())
                    : (p.getCategory() != null && targetIds.contains(p.getCategory().getId()));
            if (match) {
                eligible = eligible.add(line.getLineTotal());
            }
        }
        return amountFromBase(promo, eligible);
    }

    private static BigDecimal money(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
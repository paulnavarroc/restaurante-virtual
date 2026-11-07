package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.PromotionProduct;
import com.saboresperu.restaurante.entity.PromotionProductId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, PromotionProductId> {
    List<PromotionProduct> findByPromotionId(Integer promotionId);
    void deleteByPromotionId(Integer promotionId);
}
package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.PromotionCategory;
import com.saboresperu.restaurante.entity.PromotionCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionCategoryRepository extends JpaRepository<PromotionCategory, PromotionCategoryId> {
    List<PromotionCategory> findByPromotionId(Integer promotionId);
    void deleteByPromotionId(Integer promotionId);
}
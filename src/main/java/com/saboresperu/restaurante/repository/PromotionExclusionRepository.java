package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.PromotionExclusion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionExclusionRepository extends JpaRepository<PromotionExclusion, Integer> {
    List<PromotionExclusion> findByPromotionId(Integer promotionId);
}
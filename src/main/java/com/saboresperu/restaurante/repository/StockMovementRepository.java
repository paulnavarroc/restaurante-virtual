package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Integer> {
    List<StockMovement> findByBranchIdAndIngredientIdOrderByCreatedAtDesc(Integer branchId, Integer ingredientId);
}
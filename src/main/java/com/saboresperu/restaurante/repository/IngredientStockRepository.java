package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.IngredientStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IngredientStockRepository extends JpaRepository<IngredientStock, Integer> {
    Optional<IngredientStock> findByBranchIdAndIngredientId(Integer branchId, Integer ingredientId);
    List<IngredientStock> findByBranchId(Integer branchId);

    @Query("""
           SELECT s FROM IngredientStock s
           JOIN FETCH s.ingredient
           JOIN FETCH s.branch
           WHERE s.branch.id = :branchId
           ORDER BY s.ingredient.name
           """)
    List<IngredientStock> findByBranchIdWithDetails(@Param("branchId") Integer branchId);

    @Query("""
       SELECT s FROM IngredientStock s
       JOIN FETCH s.ingredient
       JOIN FETCH s.branch
       ORDER BY s.branch.name, s.ingredient.name
       """)
    List<IngredientStock> findAllWithDetails();

    @Query("""
       SELECT COUNT(s) FROM IngredientStock s
       JOIN s.ingredient i
       WHERE i.isUnlimitedStock = false
         AND (
           (s.minStockAlert IS NOT NULL AND s.quantityAvailable <= s.minStockAlert)
           OR (s.minStockAlert IS NULL AND i.minStockAlert IS NOT NULL
               AND s.quantityAvailable <= i.minStockAlert)
         )
       """)
    long countLowStock();
}
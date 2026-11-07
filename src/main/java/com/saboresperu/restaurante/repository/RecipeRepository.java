package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    Optional<Recipe> findByProductId(Integer productId);

    @Query("""
           SELECT r FROM Recipe r
           JOIN FETCH r.product
           LEFT JOIN FETCH r.items i
           LEFT JOIN FETCH i.ingredient
           WHERE r.product.id = :productId AND r.isActive = true
           """)
    Optional<Recipe> findActiveByProductIdWithItems(@Param("productId") Integer productId);
}

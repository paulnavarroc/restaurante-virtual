package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {
    List<Ingredient> findByIsActiveTrueOrderByNameAsc();
    List<Ingredient> findAllByOrderByNameAsc();
}

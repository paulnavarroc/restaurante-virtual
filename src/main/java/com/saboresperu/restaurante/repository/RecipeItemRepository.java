package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.RecipeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeItemRepository extends JpaRepository<RecipeItem, Integer> {
    List<RecipeItem> findByRecipeId(Integer recipeId);
    void deleteByRecipeId(Integer recipeId);
}

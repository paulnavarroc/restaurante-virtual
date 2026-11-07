package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.RecipeFormDto;
import com.saboresperu.restaurante.dto.RecipeItemFormDto;
import com.saboresperu.restaurante.dto.RecipeItemRowDto;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.IngredientRepository;
import com.saboresperu.restaurante.repository.ProductRepository;
import com.saboresperu.restaurante.repository.RecipeItemRepository;
import com.saboresperu.restaurante.repository.RecipeRepository;
import com.saboresperu.restaurante.service.interfaces.AdminRecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminRecipeServiceImpl implements AdminRecipeService {

    private final ProductRepository productRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeItemRepository recipeItemRepository;
    private final IngredientRepository ingredientRepository;

    @Override
    @Transactional(readOnly = true)
    public RecipeFormDto getByProductId(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        return recipeRepository.findActiveByProductIdWithItems(productId)
                .map(r -> new RecipeFormDto(
                        product.getId(),
                        product.getName(),
                        r.getId(),
                        r.getVersion(),
                        Boolean.TRUE.equals(r.getIsActive()),
                        r.getItems().stream()
                                .map(it -> new RecipeItemFormDto(
                                        it.getIngredient().getId(),
                                        it.getQuantity()))
                                .toList()
                ))
                .orElseGet(() -> new RecipeFormDto(
                        product.getId(),
                        product.getName(),
                        null,
                        1,
                        true,
                        List.of()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeItemRowDto> listItems(Integer productId) {
        return recipeRepository.findActiveByProductIdWithItems(productId)
                .map(r -> r.getItems().stream()
                        .map(it -> new RecipeItemRowDto(
                                it.getId(),
                                it.getIngredient().getId(),
                                it.getIngredient().getName(),
                                it.getIngredient().getUnit(),
                                it.getQuantity()
                        ))
                        .toList())
                .orElse(List.of());
    }

    @Override
    @Transactional
    public void saveRecipe(Integer productId, List<RecipeItemFormDto> items) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La receta debe tener al menos un insumo");
        }

        Set<Integer> seen = new HashSet<>();
        for (RecipeItemFormDto line : items) {
            if (!seen.add(line.ingredientId())) {
                throw new IllegalArgumentException("Insumo duplicado en la receta");
            }
        }

        Recipe recipe = recipeRepository.findByProductId(productId).orElse(null);
        if (recipe == null) {
            recipe = Recipe.builder()
                    .product(product)
                    .version(1)
                    .isActive(true)
                    .items(new ArrayList<>())
                    .build();
        } else {
            recipe.setVersion(recipe.getVersion() == null ? 1 : recipe.getVersion() + 1);
            recipe.setIsActive(true);
            recipe.getItems().clear();
        }

        recipe = recipeRepository.save(recipe);

        // orphanRemoval: clear + new items
        if (recipe.getItems() == null) {
            recipe.setItems(new ArrayList<>());
        }
        recipe.getItems().clear();
        recipeItemRepository.deleteByRecipeId(recipe.getId()); // si no usas orphanRemoval

        for (RecipeItemFormDto line : items) {
            Ingredient ingredient = ingredientRepository.findById(line.ingredientId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Insumo no encontrado: " + line.ingredientId()));
            if (!Boolean.TRUE.equals(ingredient.getIsActive())) {
                throw new IllegalArgumentException("Insumo inactivo: " + ingredient.getName());
            }
            RecipeItem ri = RecipeItem.builder()
                    .recipe(recipe)
                    .ingredient(ingredient)
                    .quantity(line.quantity().setScale(4, RoundingMode.HALF_UP))
                    .build();
            recipe.getItems().add(ri);
        }
        recipeRepository.save(recipe);

        // Producto con receta suele no ser stock ilimitado a nivel plato
        if (Boolean.TRUE.equals(product.getIsUnlimitedStock())) {
            product.setIsUnlimitedStock(false);
            productRepository.save(product);
        }
    }
}
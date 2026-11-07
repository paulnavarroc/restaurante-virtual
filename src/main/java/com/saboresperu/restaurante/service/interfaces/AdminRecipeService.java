package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.RecipeFormDto;
import com.saboresperu.restaurante.dto.RecipeItemFormDto;
import com.saboresperu.restaurante.dto.RecipeItemRowDto;

import java.util.List;

public interface AdminRecipeService {
    RecipeFormDto getByProductId(Integer productId);
    List<RecipeItemRowDto> listItems(Integer productId);
    void saveRecipe(Integer productId, List<RecipeItemFormDto> items);
}
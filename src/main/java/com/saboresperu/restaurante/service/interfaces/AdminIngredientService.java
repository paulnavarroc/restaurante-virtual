package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.IngredientFormDto;
import com.saboresperu.restaurante.dto.IngredientRowDto;

import java.util.List;

public interface AdminIngredientService {
    List<IngredientRowDto> listAll();
    IngredientFormDto getForm(Integer id);
    Integer create(IngredientFormDto form);
    void update(Integer id, IngredientFormDto form);
    void setActive(Integer id, boolean active);
}
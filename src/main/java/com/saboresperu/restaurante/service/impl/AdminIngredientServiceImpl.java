package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.IngredientFormDto;
import com.saboresperu.restaurante.dto.IngredientRowDto;
import com.saboresperu.restaurante.entity.Ingredient;
import com.saboresperu.restaurante.repository.IngredientRepository;
import com.saboresperu.restaurante.service.interfaces.AdminIngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminIngredientServiceImpl implements AdminIngredientService {

    private final IngredientRepository ingredientRepository;

    @Override
    @Transactional(readOnly = true)
    public List<IngredientRowDto> listAll() {
        return ingredientRepository.findAllByOrderByNameAsc().stream()
                .map(i -> new IngredientRowDto(
                        i.getId(),
                        i.getName(),
                        i.getUnit(),
                        Boolean.TRUE.equals(i.getIsUnlimitedStock()),
                        i.getMinStockAlert(),
                        Boolean.TRUE.equals(i.getIsActive())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public IngredientFormDto getForm(Integer id) {
        Ingredient i = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado"));
        return new IngredientFormDto(
                i.getId(),
                i.getName(),
                i.getUnit(),
                Boolean.TRUE.equals(i.getIsUnlimitedStock()),
                i.getMinStockAlert(),
                Boolean.TRUE.equals(i.getIsActive())
        );
    }

    @Override
    @Transactional
    public Integer create(IngredientFormDto form) {
        Ingredient i = new Ingredient();
        apply(i, form);
        return ingredientRepository.save(i).getId();
    }

    @Override
    @Transactional
    public void update(Integer id, IngredientFormDto form) {
        Ingredient i = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado"));
        apply(i, form);
        ingredientRepository.save(i);
    }

    @Override
    @Transactional
    public void setActive(Integer id, boolean active) {
        Ingredient i = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado"));
        i.setIsActive(active);
        ingredientRepository.save(i);
    }

    private void apply(Ingredient i, IngredientFormDto form) {
        i.setName(form.name().trim());
        i.setUnit(form.unit().trim());
        i.setIsUnlimitedStock(form.unlimitedStock());
        i.setMinStockAlert(form.minStockAlert() != null
                ? form.minStockAlert().setScale(4, RoundingMode.HALF_UP) : null);
        i.setIsActive(form.active());
    }
}
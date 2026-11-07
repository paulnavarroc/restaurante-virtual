package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.PromotionFormDto;
import com.saboresperu.restaurante.dto.PromotionRowDto;

import java.util.List;

public interface AdminPromotionService {
    List<PromotionRowDto> listAll();
    PromotionFormDto getForm(Integer id);
    Integer create(PromotionFormDto form, Integer userId);
    void update(Integer id, PromotionFormDto form, Integer userId);
    void setActive(Integer id, boolean active, Integer userId);
}
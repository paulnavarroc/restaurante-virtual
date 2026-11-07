package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.ProductCardDto;

import java.util.List;

public interface ProductService {
    List<ProductCardDto> listAvailable(Integer regionId, Integer categoryId, String q);
}
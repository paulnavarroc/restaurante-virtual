package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.ProductAdminRowDto;
import com.saboresperu.restaurante.dto.ProductFormDto;

import java.util.List;

public interface AdminProductService {
    List<ProductAdminRowDto> listAll(Boolean activeOnly);
    ProductFormDto getForm(Integer id);
    Integer create(ProductFormDto form);
    void update(Integer id, ProductFormDto form);
    void setActive(Integer id, boolean active);
}
package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.ProductAdminRowDto;
import com.saboresperu.restaurante.dto.ProductFormDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminProductService {
    List<ProductAdminRowDto> listAll(Boolean activeOnly);
    ProductFormDto getForm(Integer id);
    Integer create(ProductFormDto form, MultipartFile imageFile);
    void update(Integer id, ProductFormDto form, MultipartFile imageFile);
    void setActive(Integer id, boolean active);
}
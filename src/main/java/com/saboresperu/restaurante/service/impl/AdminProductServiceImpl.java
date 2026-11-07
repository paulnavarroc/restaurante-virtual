package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.ProductAdminRowDto;
import com.saboresperu.restaurante.dto.ProductFormDto;
import com.saboresperu.restaurante.entity.Category;
import com.saboresperu.restaurante.entity.Product;
import com.saboresperu.restaurante.entity.Region;
import com.saboresperu.restaurante.repository.CategoryRepository;
import com.saboresperu.restaurante.repository.ProductRepository;
import com.saboresperu.restaurante.repository.RegionRepository;
import com.saboresperu.restaurante.service.interfaces.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RegionRepository regionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductAdminRowDto> listAll(Boolean activeOnly) {
        List<Product> list = Boolean.TRUE.equals(activeOnly)
                ? productRepository.findAllActiveWithDetails()
                : productRepository.findAllWithDetails();
        return list.stream().map(this::toRow).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductFormDto getForm(Integer id) {
        Product p = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        return new ProductFormDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getCategory().getId(),
                p.getRegion() != null ? p.getRegion().getId() : null,
                p.getBasePrice(),
                p.getImageUrl(),
                p.getPrepTimeMinutes(),
                Boolean.TRUE.equals(p.getIsUnlimitedStock()),
                Boolean.TRUE.equals(p.getIsActive())
        );
    }

    @Override
    @Transactional
    public Integer create(ProductFormDto form) {
        Product p = mapNew(form);
        return productRepository.save(p).getId();
    }

    @Override
    @Transactional
    public void update(Integer id, ProductFormDto form) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        apply(p, form);
        productRepository.save(p);
    }

    @Override
    @Transactional
    public void setActive(Integer id, boolean active) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        p.setIsActive(active);
        productRepository.save(p);
    }

    private Product mapNew(ProductFormDto form) {
        Product p = new Product();
        p.setCreatedAt(LocalDateTime.now());
        apply(p, form);
        return p;
    }

    private void apply(Product p, ProductFormDto form) {
        Category category = categoryRepository.findById(form.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no válida"));
        Region region = null;
        if (form.regionId() != null) {
            region = regionRepository.findById(form.regionId())
                    .orElseThrow(() -> new IllegalArgumentException("Región no válida"));
        }

        p.setName(form.name().trim());
        p.setDescription(blankToNull(form.description()));
        p.setCategory(category);
        p.setRegion(region);
        p.setBasePrice(form.basePrice().setScale(2, RoundingMode.HALF_UP));
        p.setImageUrl(blankToNull(form.imageUrl()));
        p.setPrepTimeMinutes(form.prepTimeMinutes());
        p.setIsUnlimitedStock(form.unlimitedStock());
        p.setIsActive(form.active());
    }

    private ProductAdminRowDto toRow(Product p) {
        return new ProductAdminRowDto(
                p.getId(),
                p.getName(),
                p.getCategory() != null ? p.getCategory().getName() : "—",
                p.getRegion() != null ? p.getRegion().getName() : "—",
                p.getBasePrice(),
                Boolean.TRUE.equals(p.getIsActive())
        );
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
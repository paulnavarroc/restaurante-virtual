package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.ProductCardDto;
import com.saboresperu.restaurante.entity.Product;
import com.saboresperu.restaurante.repository.ProductRepository;
import com.saboresperu.restaurante.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductCardDto> listAvailable(Integer regionId, Integer categoryId, String q) {
        String query = (q == null || q.isBlank()) ? null : q.trim();

        return productRepository.findDisponibles(regionId, categoryId, query).stream()
                .map(this::toCard)
                .toList();
    }

    private ProductCardDto toCard(Product p) {
        java.math.BigDecimal list = p.getBasePrice() != null
                ? p.getBasePrice().setScale(2, java.math.RoundingMode.HALF_UP)
                : java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);

        return new ProductCardDto(
                p.getId(),
                null,
                p.getName(),
                p.getDescription(),
                list,
                list,
                false,
                p.getImageUrl(),
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getRegion() != null ? p.getRegion().getName() : null,
                p.getPrepTimeMinutes(),
                Boolean.TRUE.equals(p.getIsActive()),
                false,
                null
        );
    }
}
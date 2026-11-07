package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartDto {
    private final List<CartItemDto> items = new ArrayList<>();

    public List<CartItemDto> getItems() {
        return items;
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(CartItemDto::getQuantity).sum();
    }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CartItemDto::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
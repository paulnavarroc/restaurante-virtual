package com.saboresperu.restaurante.dto;

import java.util.List;

public record CartApiResponse(
        boolean ok,
        String message,
        int cartCount,
        List<CartLineQtyDto> lines
) {
    public record CartLineQtyDto(
            Integer productId,
            Integer dailyMenuItemId,
            int quantity
    ) {}
}
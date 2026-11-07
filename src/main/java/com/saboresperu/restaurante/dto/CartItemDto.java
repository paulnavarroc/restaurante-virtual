package com.saboresperu.restaurante.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Integer productId;
    private Integer dailyMenuItemId;
    private String productName;
    private String imageUrl;
    private BigDecimal unitPrice;
    private int quantity;

    public BigDecimal getLineTotal() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
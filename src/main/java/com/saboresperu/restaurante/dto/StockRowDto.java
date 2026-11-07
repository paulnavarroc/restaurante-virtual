package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;

public record StockRowDto(
        Integer stockId,
        Integer branchId,
        String branchName,
        Integer ingredientId,
        String ingredientName,
        String unit,
        BigDecimal quantityAvailable,
        BigDecimal quantityReserved,
        BigDecimal averageCost,
        BigDecimal lastCost,
        BigDecimal minStockAlert,
        boolean lowStock
) {}
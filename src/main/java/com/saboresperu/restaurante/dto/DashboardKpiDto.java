package com.saboresperu.restaurante.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardKpiDto(
        long ordersToday,
        long ordersPendingKitchen,
        long ordersOutForDelivery,
        BigDecimal salesToday,
        long lowStockCount,
        long openDeliveries,
        List<RecentOrderDto> recentOrders
) {}
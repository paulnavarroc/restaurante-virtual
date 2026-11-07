package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.OrderStatusCodes;
import com.saboresperu.restaurante.dto.DashboardKpiDto;
import com.saboresperu.restaurante.dto.RecentOrderDto;
import com.saboresperu.restaurante.entity.Order;
import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.DeliveryRepository;
import com.saboresperu.restaurante.repository.IngredientStockRepository;
import com.saboresperu.restaurante.repository.OrderRepository;
import com.saboresperu.restaurante.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final OrderRepository orderRepository;
    private final IngredientStockRepository ingredientStockRepository;
    private final DeliveryRepository deliveryRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardKpiDto getKpis() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        long ordersToday = orderRepository.countBetween(start, end);
        BigDecimal salesToday = orderRepository.sumSalesBetween(start, end);
        if (salesToday == null) {
            salesToday = BigDecimal.ZERO;
        }

        long pendingKitchen = orderRepository.countByStatusCodes(Set.of(
                OrderStatusCodes.PENDING,
                OrderStatusCodes.CONFIRMED,
                OrderStatusCodes.IN_PREPARATION
        ));

        long outForDelivery = orderRepository.countByStatusCodes(Set.of(
                OrderStatusCodes.OUT_FOR_DELIVERY,
                OrderStatusCodes.READY
        ));

        long lowStock = ingredientStockRepository.countLowStock();
        long openDeliveries = deliveryRepository.countOpenDeliveries();

        List<RecentOrderDto> recent = orderRepository
                .findRecent(PageRequest.of(0, 8))
                .stream()
                .map(this::toRecent)
                .toList();

        return new DashboardKpiDto(
                ordersToday,
                pendingKitchen,
                outForDelivery,
                salesToday,
                lowStock,
                openDeliveries,
                recent
        );
    }

    private RecentOrderDto toRecent(Order o) {
        User u = o.getCustomer() != null ? o.getCustomer().getUser() : null;
        String name = u != null ? (u.getFirstName() + " " + u.getLastName()).trim() : "—";
        return new RecentOrderDto(
                o.getId(),
                o.getOrderStatus() != null ? o.getOrderStatus().getName() : "—",
                o.getOrderStatus() != null ? o.getOrderStatus().getCode() : "",
                o.getTotal(),
                name,
                o.getOrderedAt() != null ? o.getOrderedAt().format(FMT) : ""
        );
    }
}
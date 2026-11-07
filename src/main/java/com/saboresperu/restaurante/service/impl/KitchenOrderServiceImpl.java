package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.OrderStatusCodes;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.KitchenOrderService;
import com.saboresperu.restaurante.service.interfaces.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KitchenOrderServiceImpl implements KitchenOrderService {

    private static final Set<String> KITCHEN_VISIBLE = Set.of(
            "PENDING", "CONFIRMED", "IN_PREPARATION", "READY"
    );

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final UserRepository userRepository;
    private final StockService stockService;
    private final Object deliveryService;

    @Override
    @Transactional(readOnly = true)
    public List<Order> listActiveForBranch(Integer branchId) {
        return orderRepository.findByBranchIdWithStatus(branchId).stream()
                .filter(o -> KITCHEN_VISIBLE.contains(o.getOrderStatus().getCode()))
                .toList();
    }

    @Override
    @Transactional
    public void updateStatus(Integer orderId, Integer branchId, String newStatusCode, Integer userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        if (!order.getBranch().getId().equals(branchId)) {
            throw new IllegalArgumentException("Pedido de otra sede");
        }

        OrderStatus next = orderStatusRepository.findByCode(newStatusCode)
                .orElseThrow(() -> new IllegalArgumentException("Estado inválido"));

        String current = order.getOrderStatus().getCode();
        validateTransition(current, newStatusCode);

        if ("IN_PREPARATION".equals(newStatusCode) && !"IN_PREPARATION".equals(current)) {
            // Primera vez a preparación: consumir reserva
            if (reservationStillReserved(order.getId())) {
                stockService.consumeReservations(order);
            }
        }

        order.setOrderStatus(next);
        orderRepository.save(order);

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        orderHistoryRepository.save(OrderHistory.builder()
                .order(order)
                .orderStatus(next)
                .user(user)
                .notes("Cocina: " + current + " → " + newStatusCode)
                .createdAt(LocalDateTime.now())
                .build());

        if (OrderStatusCodes.READY.equals(newStatusCode)) {
            deliveryService.ensurePendingDelivery(order);
        }

    }

    private boolean reservationStillReserved(Integer orderId) {
        return !reservationRepository.findByOrderIdAndStatus(
                orderId, StockReservationStatuses.RESERVED).isEmpty();
    }

    // inject OrderStockReservationRepository reservationRepository

    private void validateTransition(String from, String to) {
        boolean ok = switch (from) {
            case "PENDING" -> Set.of("CONFIRMED", "IN_PREPARATION", "CANCELLED").contains(to);
            case "CONFIRMED" -> Set.of("IN_PREPARATION", "CANCELLED").contains(to);
            case "IN_PREPARATION" -> Set.of("READY", "CANCELLED").contains(to);
            case "READY" -> Set.of("OUT_FOR_DELIVERY").contains(to);
            default -> false;
        };
        if (!ok) {
            throw new IllegalArgumentException("Transición no permitida: " + from + " → " + to);
        }
    }
}
package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.CourierStatuses;
import com.saboresperu.restaurante.domain.DeliveryStatuses;
import com.saboresperu.restaurante.domain.OrderStatusCodes;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final CourierRepository courierRepository;

    @Override
    @Transactional
    public void ensurePendingDelivery(Order order) {
        deliveryRepository.findByOrderId(order.getId()).ifPresentOrElse(
                d -> { /* ya existe */ },
                () -> deliveryRepository.save(Delivery.builder()
                        .order(order)
                        .status(DeliveryStatuses.PENDING)
                        .build())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Delivery> listPendingForBranch(Integer branchId) {
        return deliveryRepository.findPendingByBranchId(branchId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Delivery> listActiveForCourier(Integer courierId) {
        return deliveryRepository.findActiveByCourierId(courierId);
    }

    @Override
    @Transactional
    public Delivery claim(Integer deliveryId, Integer courierId, Integer branchId) {
        Delivery delivery = requireDelivery(deliveryId);
        Order order = delivery.getOrder();

        if (!order.getBranch().getId().equals(branchId)) {
            throw new IllegalArgumentException("Entrega de otra sede");
        }
        if (!DeliveryStatuses.PENDING.equals(delivery.getStatus())) {
            throw new IllegalArgumentException("La entrega ya no está disponible");
        }
        if (!OrderStatusCodes.READY.equals(order.getOrderStatus().getCode())) {
            throw new IllegalArgumentException("El pedido aún no está listo para reparto");
        }

        Courier courier = requireCourier(courierId);
        if (!Boolean.TRUE.equals(courier.getIsActive())) {
            throw new IllegalArgumentException("Repartidor inactivo");
        }
        if (!courier.getBranch().getId().equals(branchId)) {
            throw new IllegalArgumentException("No perteneces a esta sede");
        }

        delivery.setCourier(courier);
        delivery.setStatus(DeliveryStatuses.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());
        deliveryRepository.save(delivery);

        courier.setStatus(CourierStatuses.ON_DELIVERY);
        courierRepository.save(courier);

        return delivery;
    }

    @Override
    @Transactional
    public Delivery startRoute(Integer deliveryId, Integer courierId) {
        Delivery delivery = requireOwned(deliveryId, courierId);
        if (!DeliveryStatuses.ASSIGNED.equals(delivery.getStatus())
                && !DeliveryStatuses.PENDING.equals(delivery.getStatus())) {
            // PENDING no debería: claim primero
            if (!DeliveryStatuses.ASSIGNED.equals(delivery.getStatus())) {
                throw new IllegalArgumentException("Estado de entrega inválido para salir a ruta");
            }
        }

        delivery.setStatus(DeliveryStatuses.ON_ROUTE);
        delivery.setPickedUpAt(LocalDateTime.now());
        deliveryRepository.save(delivery);

        Order order = delivery.getOrder();
        OrderStatus out = orderStatusRepository.findByCode(OrderStatusCodes.OUT_FOR_DELIVERY)
                .orElseThrow(() -> new IllegalStateException("Status OUT_FOR_DELIVERY not found"));
        order.setOrderStatus(out);
        orderRepository.save(order);

        orderHistoryRepository.save(OrderHistory.builder()
                .order(order)
                .orderStatus(out)
                .user(delivery.getCourier().getUser())
                .notes("En camino")
                .createdAt(LocalDateTime.now())
                .build());

        return delivery;
    }

    @Override
    @Transactional
    public Delivery markDelivered(Integer deliveryId, Integer courierId) {
        Delivery delivery = requireOwned(deliveryId, courierId);
        if (!DeliveryStatuses.ON_ROUTE.equals(delivery.getStatus())
                && !DeliveryStatuses.ASSIGNED.equals(delivery.getStatus())) {
            throw new IllegalArgumentException("No se puede marcar entregado en este estado");
        }

        delivery.setStatus(DeliveryStatuses.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());
        deliveryRepository.save(delivery);

        Order order = delivery.getOrder();
        OrderStatus delivered = orderStatusRepository.findByCode(OrderStatusCodes.DELIVERED)
                .orElseThrow(() -> new IllegalStateException("Status DELIVERED not found"));
        order.setOrderStatus(delivered);
        orderRepository.save(order);

        orderHistoryRepository.save(OrderHistory.builder()
                .order(order)
                .orderStatus(delivered)
                .user(delivery.getCourier().getUser())
                .notes("Pedido entregado")
                .createdAt(LocalDateTime.now())
                .build());

        Courier courier = delivery.getCourier();
        long stillActive = deliveryRepository.countActiveByCourierId(courier.getId());
        if (stillActive == 0) {
            courier.setStatus(CourierStatuses.AVAILABLE);
            courierRepository.save(courier);
        }

        return delivery;
    }

    @Override
    @Transactional
    public Delivery markFailed(Integer deliveryId, Integer courierId, String notes) {
        Delivery delivery = requireOwned(deliveryId, courierId);
        delivery.setStatus(DeliveryStatuses.FAILED);
        delivery.setNotes(notes);
        deliveryRepository.save(delivery);

        Courier courier = delivery.getCourier();
        if (courier != null) {
            long stillActive = deliveryRepository.countActiveByCourierId(courier.getId());
            if (stillActive == 0) {
                courier.setStatus(CourierStatuses.AVAILABLE);
                courierRepository.save(courier);
            }
        }
        // Política: el pedido puede volver a READY o quedarse para reasignar; aquí solo falla la entrega
        return delivery;
    }

    private Delivery requireDelivery(Integer id) {
        return deliveryRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrega no encontrada"));
    }

    private Delivery requireOwned(Integer deliveryId, Integer courierId) {
        Delivery d = requireDelivery(deliveryId);
        if (d.getCourier() == null || !d.getCourier().getId().equals(courierId)) {
            throw new IllegalArgumentException("Esta entrega no te está asignada");
        }
        return d;
    }

    private Courier requireCourier(Integer courierId) {
        return courierRepository.findById(courierId)
                .orElseThrow(() -> new IllegalArgumentException("Repartidor no encontrado"));
    }
}
package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.OrderStockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStockReservationRepository extends JpaRepository<OrderStockReservation, Integer> {
    List<OrderStockReservation> findByOrderId(Integer orderId);
    List<OrderStockReservation> findByOrderIdAndStatus(Integer orderId, String status);
}

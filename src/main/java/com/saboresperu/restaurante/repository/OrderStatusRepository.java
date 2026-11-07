package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Integer> {
    Optional<OrderStatus> findByCode(String code);
    List<OrderStatus> findByIsActiveTrueOrderBySortOrderAsc();
}
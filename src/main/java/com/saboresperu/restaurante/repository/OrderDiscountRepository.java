package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.OrderDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDiscountRepository extends JpaRepository<OrderDiscount, Integer> {
    List<OrderDiscount> findByOrderId(Integer orderId);
}
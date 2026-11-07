package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Integer> {
    List<OrderHistory> findByOrderIdOrderByCreatedAtAsc(Integer orderId);

    @Query("""
       SELECT h FROM OrderHistory h
       JOIN FETCH h.orderStatus
       LEFT JOIN FETCH h.user
       WHERE h.order.id = :orderId
       ORDER BY h.createdAt ASC
       """)
    List<OrderHistory> findByOrderIdWithDetails(@Param("orderId") Integer orderId);
}
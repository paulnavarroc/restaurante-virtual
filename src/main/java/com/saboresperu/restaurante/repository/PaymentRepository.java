package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByOrderId(Integer orderId);

    @Query("""
       SELECT p FROM Payment p
       JOIN FETCH p.paymentMethod
       WHERE p.order.id = :orderId
       ORDER BY p.createdAt DESC
       """)
    List<Payment> findByOrderIdWithMethod(@Param("orderId") Integer orderId);
}
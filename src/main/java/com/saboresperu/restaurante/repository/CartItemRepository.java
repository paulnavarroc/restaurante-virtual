package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCustomerIdOrderByUpdatedAtDesc(Integer customerId);
    void deleteByCustomerId(Integer customerId);
}
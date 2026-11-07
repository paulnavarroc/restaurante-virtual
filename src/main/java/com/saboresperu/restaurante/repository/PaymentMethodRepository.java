package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {
    List<PaymentMethod> findByIsActiveTrueOrderBySortOrderAsc();
    Optional<PaymentMethod> findByCode(String code);
}
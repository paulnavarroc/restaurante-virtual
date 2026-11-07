package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {
    Optional<Coupon> findByCode(String code);
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);
    List<Coupon> findAllByOrderByStartsAtDesc();
}
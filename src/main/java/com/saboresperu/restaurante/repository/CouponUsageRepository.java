package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Integer> {
    long countByCouponIdAndUserId(Integer couponId, Integer userId);
}
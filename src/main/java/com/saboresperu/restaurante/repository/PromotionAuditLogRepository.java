package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.PromotionAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionAuditLogRepository extends JpaRepository<PromotionAuditLog, Long> {
    List<PromotionAuditLog> findByPromotionIdOrderByCreatedAtDesc(Integer promotionId);
}
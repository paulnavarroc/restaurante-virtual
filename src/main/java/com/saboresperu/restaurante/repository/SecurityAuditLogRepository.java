package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.SecurityAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {
    List<SecurityAuditLog> findTop100ByOrderByCreatedAtDesc();

    @Query("""
       SELECT s FROM SecurityAuditLog s
       LEFT JOIN FETCH s.user
       ORDER BY s.createdAt DESC
       """)
    List<SecurityAuditLog> findTopNWithUser(Pageable pageable);

    @Query("""
       SELECT s FROM SecurityAuditLog s
       LEFT JOIN FETCH s.user
       ORDER BY s.createdAt DESC
       """)
    Page<SecurityAuditLog> findAllPaged(Pageable pageable);
}
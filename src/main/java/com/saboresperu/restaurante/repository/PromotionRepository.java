package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    List<Promotion> findByIsActiveTrueAndStartsAtBeforeAndEndsAtAfter(LocalDateTime now1, LocalDateTime now2);

    List<Promotion> findAllByOrderByStartsAtDesc();

    @Query("""
       SELECT p FROM Promotion p
       WHERE p.isActive = true
         AND p.isAutomatic = true
         AND p.startsAt <= :now AND p.endsAt >= :now
         AND (p.branch IS NULL OR p.branch.id = :branchId)
       ORDER BY p.priority ASC
       """)
    List<Promotion> findActiveAutomatic(@Param("now") LocalDateTime now,
                                        @Param("branchId") Integer branchId);
}
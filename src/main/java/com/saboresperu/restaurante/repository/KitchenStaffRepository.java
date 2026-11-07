package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.KitchenStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface KitchenStaffRepository extends JpaRepository<KitchenStaff, Integer> {
    Optional<KitchenStaff> findByUserId(Integer userId);

    @Query("""
           SELECT k FROM KitchenStaff k
           JOIN FETCH k.branch
           JOIN FETCH k.user
           WHERE k.user.id = :userId AND k.isActive = true
           """)
    Optional<KitchenStaff> findActiveByUserId(@Param("userId") Integer userId);
}
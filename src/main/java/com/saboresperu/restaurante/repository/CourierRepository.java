package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourierRepository extends JpaRepository<Courier, Integer> {
    Optional<Courier> findByUserId(Integer userId);

    @Query("""
           SELECT c FROM Courier c
           JOIN FETCH c.user
           JOIN FETCH c.branch
           WHERE c.branch.id = :branchId
             AND c.isActive = true
             AND c.status = :status
           """)
    List<Courier> findDisponiblesByBranch(@Param("branchId") Integer branchId, @Param("status") String status);
}
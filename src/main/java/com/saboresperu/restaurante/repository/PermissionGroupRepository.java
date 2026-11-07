package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.PermissionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, Integer> {
    Optional<PermissionGroup> findByCode(String code);
    List<PermissionGroup> findByIsActiveTrueOrderByNameAsc();
}
package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    Optional<Permission> findByCode(String code);
    List<Permission> findByIsActiveTrueOrderByModuleAscCodeAsc();
    List<Permission> findByModuleOrderByCodeAsc(String module);
}
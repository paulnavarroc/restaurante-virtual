package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.UserPermissionConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPermissionConfigRepository extends JpaRepository<UserPermissionConfig, Integer> {
    Optional<UserPermissionConfig> findByUserId(Integer userId);
    void deleteByUserId(Integer userId);
}

package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.UserPermissionOverride;
import com.saboresperu.restaurante.entity.UserPermissionOverrideId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserPermissionOverrideRepository extends JpaRepository<UserPermissionOverride, UserPermissionOverrideId> {

    @Query("""
           SELECT o FROM UserPermissionOverride o
           JOIN FETCH o.permission
           WHERE o.userId = :userId
           """)
    List<UserPermissionOverride> findByUserIdWithPermission(@Param("userId") Integer userId);

    void deleteByUserId(Integer userId);

    List<UserPermissionOverride> findByUserId(Integer userId);
}
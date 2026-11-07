package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Permission;
import com.saboresperu.restaurante.entity.PermissionGroupItem;
import com.saboresperu.restaurante.entity.PermissionGroupItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PermissionGroupItemRepository extends JpaRepository<PermissionGroupItem, PermissionGroupItemId> {

    @Query("""
           SELECT p FROM PermissionGroupItem i
           JOIN i.permission p
           WHERE i.permissionGroupId = :groupId
           """)
    List<Permission> findPermissionsByGroupId(@Param("groupId") Integer groupId);

    void deleteByPermissionGroupId(Integer groupId);

    long countByPermissionGroupId(Integer groupId);

    List<PermissionGroupItem> findByPermissionGroupId(Integer groupId);
}
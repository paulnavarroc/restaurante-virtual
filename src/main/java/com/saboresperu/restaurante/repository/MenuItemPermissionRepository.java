package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.MenuItemPermission;
import com.saboresperu.restaurante.entity.MenuItemPermissionId;
import com.saboresperu.restaurante.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuItemPermissionRepository extends JpaRepository<MenuItemPermission, MenuItemPermissionId> {

    @Query("""
           SELECT p FROM MenuItemPermission m
           JOIN m.permission p
           WHERE m.menuItemId = :menuItemId
           """)
    List<Permission> findPermissionsByMenuItemId(@Param("menuItemId") Integer menuItemId);
}

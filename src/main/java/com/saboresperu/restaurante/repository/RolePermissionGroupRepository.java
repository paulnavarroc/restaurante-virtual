package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.PermissionGroup;
import com.saboresperu.restaurante.entity.RolePermissionGroup;
import com.saboresperu.restaurante.entity.RolePermissionGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolePermissionGroupRepository extends JpaRepository<RolePermissionGroup, RolePermissionGroupId> {

    @Query("""
           SELECT g FROM RolePermissionGroup r
           JOIN r.permissionGroup g
           WHERE r.roleId = :roleId
           """)
    List<PermissionGroup> findGroupsByRoleId(@Param("roleId") Integer roleId);

    void deleteByRoleId(Integer roleId);

    long countByRoleId(Integer roleId);

    List<RolePermissionGroup> findByRoleId(Integer roleId);
}

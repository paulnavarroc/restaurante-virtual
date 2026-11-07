package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.RolePermission;
import com.saboresperu.restaurante.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    @Query("""
           SELECT rp FROM RolePermission rp
           JOIN FETCH rp.permission
           WHERE rp.roleId = :roleId
           """)
    List<RolePermission> findByRoleIdWithPermission(@Param("roleId") Integer roleId);

    void deleteByRoleId(Integer roleId);

    List<RolePermission> findByRoleId(Integer roleId);
}

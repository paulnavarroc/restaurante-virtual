package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.*;

import java.util.List;

public interface AdminAclService {
    List<RoleAdminRowDto> listRoles();
    RoleAdminFormDto getRoleForm(Integer id);
    Integer createRole(RoleAdminFormDto form, Integer actorUserId);
    void updateRole(Integer id, RoleAdminFormDto form, Integer actorUserId);
    void setRoleActive(Integer id, boolean active, Integer actorUserId);

    List<PermissionGroupRowDto> listGroups();
    PermissionGroupFormDto getGroupForm(Integer id);
    Integer createGroup(PermissionGroupFormDto form, Integer actorUserId);
    void updateGroup(Integer id, PermissionGroupFormDto form, Integer actorUserId);

    List<PermissionRowDto> listPermissions();
}
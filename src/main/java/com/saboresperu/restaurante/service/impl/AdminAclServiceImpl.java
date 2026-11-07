package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.*;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.AdminAclService;
import com.saboresperu.restaurante.service.interfaces.SecurityConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAclServiceImpl implements AdminAclService {

    private final RoleRepository roleRepository;
    private final PermissionGroupRepository permissionGroupRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionGroupRepository rolePermissionGroupRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionGroupItemRepository permissionGroupItemRepository;
    private final UserRepository userRepository;
    private final SecurityAuditLogRepository securityAuditLogRepository;
    private final SecurityConfigService securityConfigService;

    // ---------- Roles ----------

    @Override
    @Transactional(readOnly = true)
    public List<RoleAdminRowDto> listRoles() {
        return roleRepository.findAll().stream()
                .map(r -> new RoleAdminRowDto(
                        r.getId(),
                        r.getName(),
                        r.getDescription(),
                        Boolean.TRUE.equals(r.getIsActive()),
                        rolePermissionGroupRepository.countByRoleId(r.getId()),
                        userRepository.countByRoleId(r.getId()) // ver query abajo
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleAdminFormDto getRoleForm(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        Set<Integer> groupIds = rolePermissionGroupRepository.findByRoleId(id).stream()
                .map(RolePermissionGroup::getPermissionGroupId)
                .collect(Collectors.toSet());

        Set<String> overrides = rolePermissionRepository.findByRoleId(id).stream()
                .map(rp -> rp.getEffect() + ":" + rp.getPermission().getCode())
                .collect(Collectors.toSet());

        return new RoleAdminFormDto(
                role.getId(),
                role.getName(),
                role.getDescription(),
                Boolean.TRUE.equals(role.getIsActive()),
                groupIds,
                overrides
        );
    }

    @Override
    @Transactional
    public Integer createRole(RoleAdminFormDto form, Integer actorUserId) {
        if (roleRepository.findByName(form.name().trim().toUpperCase()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un rol con ese nombre");
        }
        Role role = Role.builder()
                .name(form.name().trim().toUpperCase())
                .description(form.description())
                .isActive(form.active())
                .build();
        role = roleRepository.save(role);
        syncRoleAssignments(role.getId(), form);
        audit(actorUserId, "ROLE_CREATE", "Role", role.getId().toString(), role.getName());
        securityConfigService.bumpSecurityVersion(actorUserId, "Role created: " + role.getName());
        return role.getId();
    }

    @Override
    @Transactional
    public void updateRole(Integer id, RoleAdminFormDto form, Integer actorUserId) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
        role.setDescription(form.description());
        role.setIsActive(form.active());
        // name: opcional bloquear cambio de roles sistema
        roleRepository.save(role);
        syncRoleAssignments(id, form);
        audit(actorUserId, "ROLE_UPDATE", "Role", id.toString(), role.getName());
        securityConfigService.bumpSecurityVersion(actorUserId, "Role updated: " + role.getName());
    }

    @Override
    @Transactional
    public void setRoleActive(Integer id, boolean active, Integer actorUserId) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
        role.setIsActive(active);
        roleRepository.save(role);
        audit(actorUserId, active ? "ROLE_ACTIVATE" : "ROLE_DEACTIVATE",
                "Role", id.toString(), role.getName());
        securityConfigService.bumpSecurityVersion(actorUserId, "Role active=" + active);
    }

    private void syncRoleAssignments(Integer roleId, RoleAdminFormDto form) {
        rolePermissionGroupRepository.deleteByRoleId(roleId);
        rolePermissionRepository.deleteByRoleId(roleId);

        if (form.permissionGroupIds() != null) {
            for (Integer gid : form.permissionGroupIds()) {
                if (!permissionGroupRepository.existsById(gid)) {
                    throw new IllegalArgumentException("Grupo no válido: " + gid);
                }
                rolePermissionGroupRepository.save(RolePermissionGroup.builder()
                        .roleId(roleId)
                        .permissionGroupId(gid)
                        .build());
            }
        }

        if (form.permissionOverrides() != null) {
            for (String token : form.permissionOverrides()) {
                // formato EFFECT:CODE
                String[] parts = token.split(":", 2);
                if (parts.length != 2) continue;
                String effect = parts[0].trim().toUpperCase();
                String code = parts[1].trim();
                if (!"ALLOW".equals(effect) && !"DENY".equals(effect)) continue;

                Permission perm = permissionRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException("Permiso no válido: " + code));

                rolePermissionRepository.save(RolePermission.builder()
                        .roleId(roleId)
                        .permissionId(perm.getId())
                        .effect(effect)
                        .permission(perm)
                        .build());
            }
        }
    }

    // ---------- Groups ----------

    @Override
    @Transactional(readOnly = true)
    public List<PermissionGroupRowDto> listGroups() {
        return permissionGroupRepository.findAll().stream()
                .map(g -> new PermissionGroupRowDto(
                        g.getId(),
                        g.getCode(),
                        g.getName(),
                        g.getDescription(),
                        Boolean.TRUE.equals(g.getIsActive()),
                        (int) permissionGroupItemRepository.countByPermissionGroupId(g.getId())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionGroupFormDto getGroupForm(Integer id) {
        PermissionGroup g = permissionGroupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        Set<Integer> permIds = permissionGroupItemRepository.findByPermissionGroupId(id).stream()
                .map(PermissionGroupItem::getPermissionId)
                .collect(Collectors.toSet());
        return new PermissionGroupFormDto(
                g.getId(), g.getCode(), g.getName(), g.getDescription(),
                Boolean.TRUE.equals(g.getIsActive()), permIds);
    }

    @Override
    @Transactional
    public Integer createGroup(PermissionGroupFormDto form, Integer actorUserId) {
        String code = form.code().trim().toUpperCase();
        if (permissionGroupRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Código de grupo ya existe");
        }
        PermissionGroup g = PermissionGroup.builder()
                .code(code)
                .name(form.name().trim())
                .description(form.description())
                .isActive(form.active())
                .build();
        g = permissionGroupRepository.save(g);
        syncGroupPermissions(g.getId(), form.permissionIds());
        audit(actorUserId, "GROUP_CREATE", "PermissionGroup", g.getId().toString(), code);
        securityConfigService.bumpSecurityVersion(actorUserId, "Group created: " + code);
        return g.getId();
    }

    @Override
    @Transactional
    public void updateGroup(Integer id, PermissionGroupFormDto form, Integer actorUserId) {
        PermissionGroup g = permissionGroupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        g.setName(form.name().trim());
        g.setDescription(form.description());
        g.setIsActive(form.active());
        permissionGroupRepository.save(g);
        syncGroupPermissions(id, form.permissionIds());
        audit(actorUserId, "GROUP_UPDATE", "PermissionGroup", id.toString(), g.getCode());
        securityConfigService.bumpSecurityVersion(actorUserId, "Group updated: " + g.getCode());
    }

    private void syncGroupPermissions(Integer groupId, Set<Integer> permissionIds) {
        permissionGroupItemRepository.deleteByPermissionGroupId(groupId);
        if (permissionIds == null) return;
        for (Integer pid : permissionIds) {
            if (!permissionRepository.existsById(pid)) {
                throw new IllegalArgumentException("Permiso no válido: " + pid);
            }
            permissionGroupItemRepository.save(PermissionGroupItem.builder()
                    .permissionGroupId(groupId)
                    .permissionId(pid)
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionRowDto> listPermissions() {
        return permissionRepository.findAllByOrderByModuleAscCodeAsc().stream()
                .map(p -> new PermissionRowDto(
                        p.getId(), p.getCode(), p.getModule(),
                        p.getDescription(), Boolean.TRUE.equals(p.getIsActive())))
                .toList();
    }

    private void audit(Integer actorUserId, String action, String entityType,
                       String entityId, String detail) {
        User actor = actorUserId != null
                ? userRepository.findById(actorUserId).orElse(null) : null;
        securityAuditLogRepository.save(SecurityAuditLog.builder()
                .user(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .detail(detail)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
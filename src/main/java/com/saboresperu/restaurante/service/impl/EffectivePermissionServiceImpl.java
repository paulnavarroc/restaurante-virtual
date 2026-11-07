package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.PermissionEffects;
import com.saboresperu.restaurante.domain.PermissionModes;
import com.saboresperu.restaurante.dto.MenuItemDto;
import com.saboresperu.restaurante.dto.SessionAuthorizationDto;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.EffectivePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EffectivePermissionServiceImpl implements EffectivePermissionService {

    private final UserRepository userRepository;
    private final UserPermissionConfigRepository userPermissionConfigRepository;
    private final UserPermissionOverrideRepository userPermissionOverrideRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RolePermissionGroupRepository rolePermissionGroupRepository;
    private final PermissionGroupItemRepository permissionGroupItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemPermissionRepository menuItemPermissionRepository;
    private final SecurityConfigRepository securityConfigRepository;

    @Override
    @Transactional(readOnly = true)
    public Set<String> resolvePermissionCodes(Integer userId) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Set<String> base = resolveBaseFromRoles(user.getRoles());

        String mode = userPermissionConfigRepository.findById(userId)
                .map(UserPermissionConfig::getPermissionMode)
                .orElse(PermissionModes.INHERIT);

        Set<String> userAllow = new HashSet<>();
        Set<String> userDeny = new HashSet<>();
        for (UserPermissionOverride o : userPermissionOverrideRepository.findByUserIdWithPermission(userId)) {
            if (!Boolean.TRUE.equals(o.getPermission().getIsActive())) continue;
            if (PermissionEffects.ALLOW.equals(o.getEffect())) {
                userAllow.add(o.getPermission().getCode());
            } else if (PermissionEffects.DENY.equals(o.getEffect())) {
                userDeny.add(o.getPermission().getCode());
            }
        }

        return switch (mode) {
            case PermissionModes.CUSTOM -> {
                Set<String> custom = new HashSet<>(userAllow);
                custom.removeAll(userDeny);
                yield custom;
            }
            case PermissionModes.RESTRICT -> {
                Set<String> result = new HashSet<>(base);
                result.addAll(userAllow);
                result.removeAll(userDeny);
                yield result;
            }
            default -> new HashSet<>(base);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> resolveRolePermissionCodes(Integer userId) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return resolveBaseFromRoles(user.getRoles());
    }

    private Set<String> resolveBaseFromRoles(Set<Role> roles) {
        Set<String> allowGroups = new HashSet<>();
        Set<String> allowRole = new HashSet<>();
        Set<String> denyRole = new HashSet<>();

        for (Role role : roles) {
            if (!Boolean.TRUE.equals(role.getIsActive())) continue;

            for (PermissionGroup group : rolePermissionGroupRepository.findGroupsByRoleId(role.getId())) {
                if (!Boolean.TRUE.equals(group.getIsActive())) continue;
                for (Permission p : permissionGroupItemRepository.findPermissionsByGroupId(group.getId())) {
                    if (Boolean.TRUE.equals(p.getIsActive())) {
                        allowGroups.add(p.getCode());
                    }
                }
            }

            for (RolePermission rp : rolePermissionRepository.findByRoleIdWithPermission(role.getId())) {
                if (!Boolean.TRUE.equals(rp.getPermission().getIsActive())) continue;
                if (PermissionEffects.ALLOW.equals(rp.getEffect())) {
                    allowRole.add(rp.getPermission().getCode());
                } else if (PermissionEffects.DENY.equals(rp.getEffect())) {
                    denyRole.add(rp.getPermission().getCode());
                }
            }
        }

        Set<String> base = new HashSet<>();
        base.addAll(allowGroups);
        base.addAll(allowRole);
        base.removeAll(denyRole);
        return base;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemDto> resolveAllMenus(Integer userId) {
        Set<String> perms = resolvePermissionCodes(userId);
        return menuItemRepository.findByIsActiveTrueOrderByContextAscSortOrderAsc().stream()
                .filter(item -> isVisible(item.getId(), perms))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemDto> resolveMenuByContext(Integer userId, String context) {
        return resolveAllMenus(userId).stream()
                .filter(m -> context.equals(m.context()))
                .toList();
    }

    private boolean isVisible(Integer menuItemId, Set<String> userPerms) {
        List<Permission> required = menuItemPermissionRepository.findPermissionsByMenuItemId(menuItemId);
        if (required.isEmpty()) return false;
        return required.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .map(Permission::getCode)
                .anyMatch(userPerms::contains);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionAuthorizationDto buildSessionAuthorization(Integer userId) {
        Set<String> codes = resolvePermissionCodes(userId);
        List<MenuItemDto> menus = resolveAllMenus(userId);
        return new SessionAuthorizationDto(currentSecurityVersion(), codes, menus);
    }

    @Override
    @Transactional(readOnly = true)
    public long currentSecurityVersion() {
        return securityConfigRepository.findById(1)
                .map(c -> c.getSecurityVersion().longValue())
                .orElse(1L);
    }

    private MenuItemDto toDto(MenuItem m) {
        return new MenuItemDto(
                m.getCode(),
                m.getLabel(),
                m.getRoute(),
                m.getIcon(),
                m.getMenuGroup(),
                m.getContext(),
                m.getSortOrder() != null ? m.getSortOrder() : 0
        );
    }
}
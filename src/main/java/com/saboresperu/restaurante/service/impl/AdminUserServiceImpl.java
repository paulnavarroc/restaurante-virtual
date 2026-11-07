package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.PermissionModes;
import com.saboresperu.restaurante.dto.EffectivePermissionPreviewDto;
import com.saboresperu.restaurante.dto.RoleOptionDto;
import com.saboresperu.restaurante.dto.UserAdminFormDto;
import com.saboresperu.restaurante.dto.UserAdminRowDto;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.AdminUserService;
import com.saboresperu.restaurante.service.interfaces.EffectivePermissionService;
import com.saboresperu.restaurante.service.interfaces.SecurityConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final KitchenStaffRepository kitchenStaffRepository;
    private final CourierRepository courierRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPermissionConfigRepository userPermissionConfigRepository;
    private final UserPermissionOverrideRepository userPermissionOverrideRepository;
    private final PermissionRepository permissionRepository;
    private final EffectivePermissionService effectivePermissionService;
    private final SecurityConfigService securityConfigService;

    private void syncUserPermissions(Integer userId, String mode, Set<String> overrides) {
        String resolvedMode = mode == null || mode.isBlank()
                ? PermissionModes.INHERIT : mode.trim().toUpperCase();

        if (!Set.of(PermissionModes.INHERIT, PermissionModes.RESTRICT, PermissionModes.CUSTOM)
                .contains(resolvedMode)) {
            throw new IllegalArgumentException("Modo de permisos no válido");
        }

        UserPermissionConfig cfg = userPermissionConfigRepository.findByUserId(userId)
                .orElseGet(() -> UserPermissionConfig.builder()
                        .userId(userId)
                        .build());
        cfg.setMode(resolvedMode);
        userPermissionConfigRepository.save(cfg);

        userPermissionOverrideRepository.deleteByUserId(userId);

        if (PermissionModes.INHERIT.equals(resolvedMode)) {
            return; // sin overrides
        }

        if (overrides != null) {
            for (String token : overrides) {
                String[] parts = token.split(":", 2);
                if (parts.length != 2) continue;
                String effect = parts[0].trim().toUpperCase();
                String code = parts[1].trim();
                if (!"ALLOW".equals(effect) && !"DENY".equals(effect)) continue;

                Permission perm = permissionRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException("Permiso: " + code));

                userPermissionOverrideRepository.save(UserPermissionOverride.builder()
                        .userId(userId)
                        .permissionId(perm.getId())
                        .effect(effect)
                        .build());
            }
        }

        if (PermissionModes.CUSTOM.equals(resolvedMode)
                && (overrides == null || overrides.isEmpty())) {
            throw new IllegalArgumentException(
                    "El modo Personalizado requiere al menos un permiso ALLOW");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EffectivePermissionPreviewDto previewEffective(Integer userId) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String mode = userPermissionConfigRepository.findByUserId(userId)
                .map(UserPermissionConfig::getMode)
                .orElse(PermissionModes.INHERIT);

        // Solo roles (sin overrides de usuario) — método auxiliar en EffectivePermissionService
        Set<String> fromRoles = effectivePermissionService.resolveRolePermissionCodes(userId);

        List<UserPermissionOverride> overs =
                userPermissionOverrideRepository.findByUserId(userId);

        List<String> allows = overs.stream()
                .filter(o -> "ALLOW".equals(o.getEffect()))
                .map(o -> o.getPermission().getCode())
                .sorted()
                .toList();
        List<String> denies = overs.stream()
                .filter(o -> "DENY".equals(o.getEffect()))
                .map(o -> o.getPermission().getCode())
                .sorted()
                .toList();

        Set<String> effective = effectivePermissionService.resolvePermissionCodes(userId);

        return new EffectivePermissionPreviewDto(
                mode,
                fromRoles.stream().sorted().toList(),
                allows,
                denies,
                effective.stream().sorted().toList()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAdminRowDto> search(String query) {
        List<User> users = (query == null || query.isBlank())
                ? userRepository.findAllWithRoles()
                : userRepository.searchWithRoles(query.trim());

        return users.stream().map(u -> new UserAdminRowDto(
                u.getId(),
                (u.getFirstName() + " " + u.getLastName()).trim(),
                u.getEmail(),
                u.getPhone(),
                Boolean.TRUE.equals(u.getIsActive()),
                u.getRoles().stream().map(Role::getName).sorted().toList(),
                u.getCreatedAt(),
                u.getLastAccessAt()
        )).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserAdminFormDto getForm(Integer id) {
        User u = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Integer kitchenBranchId = kitchenStaffRepository.findByUserId(id)
                .map(k -> k.getBranch().getId())
                .orElse(null);
        Integer courierBranchId = courierRepository.findByUserId(id)
                .map(c -> c.getBranch().getId())
                .orElse(null);

        Set<Integer> roleIds = u.getRoles().stream().map(Role::getId).collect(Collectors.toSet());

        return new UserAdminFormDto(
                u.getId(),
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                u.getPhone(),
                null,
                Boolean.TRUE.equals(u.getIsActive()),
                roleIds,
                kitchenBranchId,
                courierBranchId
        );
    }

    @Override
    @Transactional
    public Integer create(UserAdminFormDto form, Integer actorUserId) {
        String email = form.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");
        }
        if (form.password() == null || form.password().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        Set<Role> roles = resolveRoles(form.roleIds());
        User user = User.builder()
                .firstName(form.firstName().trim())
                .lastName(form.lastName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(form.password()))
                .phone(blankToNull(form.phone()))
                .isActive(form.active())
                .createdAt(LocalDateTime.now())
                .roles(roles)
                .build();
        user = userRepository.save(user);

        syncStaffProfiles(user, roles, form.kitchenBranchId(), form.courierBranchId());
        ensureCustomerIfNeeded(user, roles);

        syncUserPermissions(user.getId(), form.permissionMode(), form.permissionOverrides());
        securityConfigService.bumpSecurityVersion(actorUserId, "User permissions: " + user.getEmail());

        return user.getId();
    }

    @Override
    @Transactional
    public void update(Integer id, UserAdminFormDto form, Integer actorUserId) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String email = form.email().trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new IllegalArgumentException("Correo ya usado por otro usuario");
            }
        });

        user.setFirstName(form.firstName().trim());
        user.setLastName(form.lastName().trim());
        user.setEmail(email);
        user.setPhone(blankToNull(form.phone()));
        user.setIsActive(form.active());

        if (form.password() != null && !form.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(form.password()));
        }

        Set<Role> roles = resolveRoles(form.roleIds());

        boolean willBeAdmin = roles.stream().anyMatch(r -> "ADMINISTRATOR".equals(r.getName()));
        boolean wasAdmin = user.getRoles().stream().anyMatch(r -> "ADMINISTRATOR".equals(r.getName()));
        if (wasAdmin && !willBeAdmin) {
            long remaining = userRepository.countActiveByRoleNameExcluding("ADMINISTRATOR", id);
            if (remaining == 0) {
                throw new IllegalArgumentException("Debe existir al menos un ADMINISTRADOR activo");
            }
        }
        if (wasAdmin && !form.active()) {
            long remaining = userRepository.countActiveByRoleNameExcluding("ADMINISTRATOR", id);
            if (remaining == 0) {
                throw new IllegalArgumentException("No puedes desactivar el último ADMINISTRADOR");
            }
        }

        user.setRoles(roles);
        userRepository.save(user);

        syncStaffProfiles(user, roles, form.kitchenBranchId(), form.courierBranchId());
        ensureCustomerIfNeeded(user, roles);

        syncUserPermissions(user.getId(), form.permissionMode(), form.permissionOverrides());
        securityConfigService.bumpSecurityVersion(actorUserId, "User permissions: " + user.getEmail());
    }

    @Override
    @Transactional
    public void setActive(Integer id, boolean active) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ADMINISTRATOR".equals(r.getName()));
        if (isAdmin && !active) {
            long remaining = userRepository.countActiveByRoleNameExcluding("ADMINISTRATOR", id);
            if (remaining == 0) {
                throw new IllegalArgumentException(
                        "No puedes desactivar el último ADMINISTRADOR");
            }
        }

        user.setIsActive(active);
        userRepository.save(user);
        if (!active) {
            securityConfigService.bumpSecurityVersion(null, "User deactivated: " + user.getEmail());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleOptionDto> listRoles() {
        return roleRepository.findByIsActiveTrue().stream()
                .map(r -> new RoleOptionDto(r.getId(), r.getName(), r.getDescription()))
                .toList();
    }

    private Set<Role> resolveRoles(Set<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("Asigna al menos un rol");
        }
        Set<Role> roles = new HashSet<>();
        for (Integer rid : roleIds) {
            Role role = roleRepository.findById(rid)
                    .orElseThrow(() -> new IllegalArgumentException("Rol no válido: " + rid));
            if (!Boolean.TRUE.equals(role.getIsActive())) {
                throw new IllegalArgumentException("Rol inactivo: " + role.getName());
            }
            roles.add(role);
        }
        return roles;
    }

    private void syncStaffProfiles(User user, Set<Role> roles,
                                   Integer kitchenBranchId, Integer courierBranchId) {
        boolean isChef = roles.stream().anyMatch(r -> "CHEF".equals(r.getName()));
        boolean isCourier = roles.stream().anyMatch(r -> "COURIER".equals(r.getName()));

        if (isChef) {
            if (kitchenBranchId == null) {
                throw new IllegalArgumentException("El rol CHEF requiere sede de cocina");
            }
            Branch branch = branchRepository.findById(kitchenBranchId)
                    .orElseThrow(() -> new IllegalArgumentException("Sede de cocina no válida"));
            kitchenStaffRepository.findByUserId(user.getId()).ifPresentOrElse(ks -> {
                ks.setBranch(branch);
                ks.setIsActive(true);
                kitchenStaffRepository.save(ks);
            }, () -> kitchenStaffRepository.save(KitchenStaff.builder()
                    .user(user)
                    .branch(branch)
                    .jobTitle("Chef")
                    .isActive(true)
                    .assignedAt(LocalDateTime.now())
                    .build()));
        } else {
            kitchenStaffRepository.findByUserId(user.getId()).ifPresent(ks -> {
                ks.setIsActive(false);
                kitchenStaffRepository.save(ks);
            });
        }

        if (isCourier) {
            if (courierBranchId == null) {
                throw new IllegalArgumentException("El rol COURIER requiere sede de reparto");
            }
            Branch branch = branchRepository.findById(courierBranchId)
                    .orElseThrow(() -> new IllegalArgumentException("Sede de reparto no válida"));
            courierRepository.findByUserId(user.getId()).ifPresentOrElse(c -> {
                c.setBranch(branch);
                c.setIsActive(true);
                c.setPhone(user.getPhone());
                courierRepository.save(c);
            }, () -> courierRepository.save(Courier.builder()
                    .user(user)
                    .branch(branch)
                    .phone(user.getPhone())
                    .vehicle("Moto")
                    .status("AVAILABLE")
                    .isActive(true)
                    .build()));
        } else {
            courierRepository.findByUserId(user.getId()).ifPresent(c -> {
                c.setIsActive(false);
                courierRepository.save(c);
            });
        }
    }

    private void ensureCustomerIfNeeded(User user, Set<Role> roles) {
        boolean isCustomer = roles.stream().anyMatch(r -> "CUSTOMER".equals(r.getName()));
        if (!isCustomer) {
            return;
        }
        if (customerRepository.findByUserId(user.getId()).isEmpty()) {
            customerRepository.save(Customer.builder()
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
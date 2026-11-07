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

    private static final String ROLE_ADMINISTRATOR = "ADMINISTRATOR";
    private static final String ROLE_CHEF = "CHEF";
    private static final String ROLE_COURIER = "COURIER";
    private static final String ROLE_CUSTOMER = "CUSTOMER";

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

    // -------------------------------------------------------------------------
    // AdminUserService
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<UserAdminRowDto> search(String query) {
        List<User> users = (query == null || query.isBlank())
                ? userRepository.findAllWithRoles()
                : userRepository.searchWithRoles(query.trim());

        return users.stream()
                .map(u -> new UserAdminRowDto(
                        u.getId(),
                        (u.getFirstName() + " " + u.getLastName()).trim(),
                        u.getEmail(),
                        u.getPhone(),
                        Boolean.TRUE.equals(u.getIsActive()),
                        u.getRoles().stream().map(Role::getName).sorted().toList(),
                        u.getCreatedAt(),
                        u.getLastAccessAt()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserAdminFormDto getForm(Integer id) {
        User u = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Integer kitchenBranchId = kitchenStaffRepository.findByUserId(id)
                .map(k -> k.getBranch() != null ? k.getBranch().getId() : null)
                .orElse(null);

        Integer courierBranchId = courierRepository.findByUserId(id)
                .map(c -> c.getBranch() != null ? c.getBranch().getId() : null)
                .orElse(null);

        Set<Integer> roleIds = u.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        String mode = userPermissionConfigRepository.findByUserId(id)
                .map(UserPermissionConfig::getPermissionMode)
                .orElse(PermissionModes.INHERIT);

        Set<String> overrides = userPermissionOverrideRepository
                .findByUserIdWithPermission(id)
                .stream()
                .filter(o -> o.getPermission() != null && o.getPermission().getCode() != null)
                .map(o -> o.getEffect() + ":" + o.getPermission().getCode())
                .collect(Collectors.toCollection(LinkedHashSet::new));

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
                courierBranchId,
                mode,
                overrides
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
        syncUserPermissions(user, form.permissionMode(), form.permissionOverrides());

        securityConfigService.bumpSecurityVersion(
                actorUserId, "User created: " + user.getEmail());

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

        Set<Role> newRoles = resolveRoles(form.roleIds());
        assertNotRemovingLastAdministrator(user, newRoles, form.active());

        user.setFirstName(form.firstName().trim());
        user.setLastName(form.lastName().trim());
        user.setEmail(email);
        user.setPhone(blankToNull(form.phone()));
        user.setIsActive(form.active());

        if (form.password() != null && !form.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(form.password()));
        }

        user.setRoles(newRoles);
        userRepository.save(user);

        syncStaffProfiles(user, newRoles, form.kitchenBranchId(), form.courierBranchId());
        ensureCustomerIfNeeded(user, newRoles);
        syncUserPermissions(user, form.permissionMode(), form.permissionOverrides());

        securityConfigService.bumpSecurityVersion(
                actorUserId, "User updated: " + user.getEmail());
    }

    @Override
    @Transactional
    public void setActive(Integer id, boolean active) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!active) {
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(r -> ROLE_ADMINISTRATOR.equals(r.getName()));
            if (isAdmin) {
                long remaining = userRepository
                        .countActiveByRoleNameExcluding(ROLE_ADMINISTRATOR, id);
                if (remaining == 0) {
                    throw new IllegalArgumentException(
                            "No puedes desactivar el último ADMINISTRADOR");
                }
            }
        }

        user.setIsActive(active);
        userRepository.save(user);

        securityConfigService.bumpSecurityVersion(
                null, "User active=" + active + ": " + user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleOptionDto> listRoles() {
        return roleRepository.findByIsActiveTrue().stream()
                .map(r -> new RoleOptionDto(r.getId(), r.getName(), r.getDescription()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EffectivePermissionPreviewDto previewEffective(Integer userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        String mode = userPermissionConfigRepository.findByUserId(userId)
                .map(UserPermissionConfig::getPermissionMode)
                .orElse(PermissionModes.INHERIT);

        Set<String> fromRoles = effectivePermissionService.resolveRolePermissionCodes(userId);

        List<UserPermissionOverride> overs =
                userPermissionOverrideRepository.findByUserIdWithPermission(userId);

        List<String> allows = overs.stream()
                .filter(o -> "ALLOW".equalsIgnoreCase(o.getEffect()))
                .map(o -> o.getPermission() != null ? o.getPermission().getCode() : null)
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        List<String> denies = overs.stream()
                .filter(o -> "DENY".equalsIgnoreCase(o.getEffect()))
                .map(o -> o.getPermission() != null ? o.getPermission().getCode() : null)
                .filter(Objects::nonNull)
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

    // -------------------------------------------------------------------------
    // Private
    // -------------------------------------------------------------------------

    private void syncUserPermissions(User user, String mode, Set<String> overrides) {
        Integer userId = user.getId();

        String resolvedMode = mode == null || mode.isBlank()
                ? PermissionModes.INHERIT
                : mode.trim().toUpperCase();

        if (!Set.of(PermissionModes.INHERIT, PermissionModes.RESTRICT, PermissionModes.CUSTOM)
                .contains(resolvedMode)) {
            throw new IllegalArgumentException("Modo de permisos no válido");
        }

        UserPermissionConfig cfg = userPermissionConfigRepository.findByUserId(userId)
                .orElseGet(() -> UserPermissionConfig.builder()
                        .user(user)
                        .permissionMode(PermissionModes.INHERIT)
                        .updatedAt(LocalDateTime.now())
                        .build());

        cfg.setPermissionMode(resolvedMode);
        cfg.setUpdatedAt(LocalDateTime.now());
        userPermissionConfigRepository.save(cfg);

        userPermissionOverrideRepository.deleteByUserId(userId);

        if (PermissionModes.INHERIT.equals(resolvedMode)) {
            return;
        }

        boolean hasAllow = false;
        if (overrides != null) {
            for (String token : overrides) {
                if (token == null || token.isBlank()) {
                    continue;
                }
                String[] parts = token.split(":", 2);
                if (parts.length != 2) {
                    continue;
                }
                String effect = parts[0].trim().toUpperCase();
                String code = parts[1].trim();
                if (!"ALLOW".equals(effect) && !"DENY".equals(effect)) {
                    continue;
                }

                Permission perm = permissionRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException("Permiso: " + code));

                userPermissionOverrideRepository.save(UserPermissionOverride.builder()
                        .userId(userId)
                        .permissionId(perm.getId())
                        .effect(effect)
                        .build());

                if ("ALLOW".equals(effect)) {
                    hasAllow = true;
                }
            }
        }

        if (PermissionModes.CUSTOM.equals(resolvedMode) && !hasAllow) {
            throw new IllegalArgumentException(
                    "El modo Personalizado requiere al menos un permiso ALLOW");
        }
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

    private void assertNotRemovingLastAdministrator(User user, Set<Role> newRoles, boolean active) {
        boolean wasAdmin = user.getRoles().stream()
                .anyMatch(r -> ROLE_ADMINISTRATOR.equals(r.getName()));
        if (!wasAdmin) {
            return;
        }

        boolean willBeAdmin = newRoles.stream()
                .anyMatch(r -> ROLE_ADMINISTRATOR.equals(r.getName()));

        if (!willBeAdmin || !active) {
            long remaining = userRepository
                    .countActiveByRoleNameExcluding(ROLE_ADMINISTRATOR, user.getId());
            if (remaining == 0) {
                throw new IllegalArgumentException(
                        "Debe existir al menos un ADMINISTRADOR activo");
            }
        }
    }

    private void syncStaffProfiles(User user, Set<Role> roles,
                                   Integer kitchenBranchId, Integer courierBranchId) {
        boolean isChef = roles.stream().anyMatch(r -> ROLE_CHEF.equals(r.getName()));
        boolean isCourier = roles.stream().anyMatch(r -> ROLE_COURIER.equals(r.getName()));

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
        boolean isCustomer = roles.stream().anyMatch(r -> ROLE_CUSTOMER.equals(r.getName()));
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
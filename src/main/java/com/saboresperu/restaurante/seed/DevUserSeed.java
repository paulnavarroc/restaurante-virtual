package com.saboresperu.restaurante.seed;

import com.saboresperu.restaurante.domain.CourierStatuses;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Demo users for profile "dev". Requires V1 schema + V1_1 security seed.
 * Password for all: 123456
 */
@Component
@Profile("dev")
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class DevUserSeed implements CommandLineRunner {

    private static final String PASSWORD = "123456";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final GeoDistrictRepository geoDistrictRepository;
    private final BranchRepository branchRepository;
    private final KitchenStaffRepository kitchenStaffRepository;
    private final CourierRepository courierRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== DevUserSeed ===");

        User admin = ensureUser("admin@saboresperu.com", "Carlos", "Admin", "999000001", "ADMINISTRATOR");
        ensureUser("ops@saboresperu.com", "Lucia", "Operations", "999000002", "OPERATIONS");
        User chef = ensureUser("cocina@saboresperu.com", "Marco", "Chef", "999000003", "CHEF");
        User courierUser = ensureUser("repartidor@saboresperu.com", "Pedro", "Courier", "999000004", "COURIER");
        User customerUser = ensureUser("cliente@saboresperu.com", "Ana", "Cliente", "999111222", "CUSTOMER");

        ensureCustomerAndAddress(customerUser);
        ensureKitchenStaff(chef);
        ensureCourier(courierUser);

        log.info("Demo users ready (password: {})", PASSWORD);
        log.info("  admin@saboresperu.com      → ADMINISTRATOR");
        log.info("  ops@saboresperu.com        → OPERATIONS");
        log.info("  cocina@saboresperu.com     → CHEF");
        log.info("  repartidor@saboresperu.com → COURIER");
        log.info("  cliente@saboresperu.com    → CUSTOMER");
        if (admin != null) {
            // keep reference used
        }
    }

    private User ensureUser(String email, String firstName, String lastName,
                            String phone, String roleName) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalStateException(
                            "Role not found: " + roleName + ". Run V1 / V1_1 SQL seeds."));

            Set<Role> roles = new HashSet<>();
            roles.add(role);

            User user = User.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email.trim().toLowerCase())
                    .passwordHash(passwordEncoder.encode(PASSWORD))
                    .phone(phone)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .roles(roles)
                    .build();

            user = userRepository.save(user);
            log.info("Created user {} ({})", email, roleName);
            return user;
        });
    }

    private void ensureCustomerAndAddress(User user) {
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .user(user)
                        .documentType("DNI")
                        .documentNumber("12345678")
                        .createdAt(LocalDateTime.now())
                        .build()));

        if (!customerAddressRepository.findByCustomerIdAndIsActiveTrueOrderByIsPrimaryDesc(customer.getId()).isEmpty()) {
            return;
        }

        GeoDistrict district = geoDistrictRepository.findById("150122")
                .or(() -> geoDistrictRepository.findAll().stream().findFirst())
                .orElse(null);

        if (district == null) {
            log.warn("No GeoDistrict found. Skip customer address seed.");
            return;
        }

        customerAddressRepository.save(CustomerAddress.builder()
                .customer(customer)
                .district(district)
                .alias("Casa")
                .street("Av. Benavides")
                .number("500")
                .reference("Near oval")
                .lat(district.getCenterLat() != null ? district.getCenterLat() : new BigDecimal("-12.1250000"))
                .lng(district.getCenterLng() != null ? district.getCenterLng() : new BigDecimal("-77.0200000"))
                .isPrimary(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build());

        log.info("Customer address created for {}", user.getEmail());
    }

    private void ensureKitchenStaff(User user) {
        if (kitchenStaffRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }
        Branch branch = firstActiveBranch();
        if (branch == null) {
            log.warn("No active branch. Skip KitchenStaff seed.");
            return;
        }
        kitchenStaffRepository.save(KitchenStaff.builder()
                .user(user)
                .branch(branch)
                .jobTitle("Chef")
                .isActive(true)
                .assignedAt(LocalDateTime.now())
                .build());
        log.info("KitchenStaff linked to branch {}", branch.getName());
    }

    private void ensureCourier(User user) {
        if (courierRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }
        Branch branch = firstActiveBranch();
        if (branch == null) {
            log.warn("No active branch. Skip Courier seed.");
            return;
        }
        courierRepository.save(Courier.builder()
                .user(user)
                .branch(branch)
                .phone(user.getPhone())
                .vehicle("Moto")
                .status(CourierStatuses.AVAILABLE)
                .isActive(true)
                .lastLat(branch.getLat())
                .lastLng(branch.getLng())
                .build());
        log.info("Courier linked to branch {}", branch.getName());
    }

    private Branch firstActiveBranch() {
        List<Branch> list = branchRepository.findByIsActiveTrue();
        return list.isEmpty() ? null : list.get(0);
    }
}
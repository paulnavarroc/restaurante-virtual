package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.RegisterRequestDto;
import com.saboresperu.restaurante.entity.Customer;
import com.saboresperu.restaurante.entity.Role;
import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.CustomerRepository;
import com.saboresperu.restaurante.repository.RoleRepository;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String ROLE_CUSTOMER = "CUSTOMER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerCustomer(RegisterRequestDto form) {
        String email = form.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo");
        }

        if (!form.password().equals(form.passwordConfirm())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        Role customerRole = roleRepository.findByName(ROLE_CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("Role CUSTOMER not found. Run SQL seeds."));

        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);

        User user = User.builder()
                .firstName(form.firstName().trim())
                .lastName(form.lastName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(form.password()))
                .phone(blankToNull(form.phone()))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .roles(roles)
                .build();

        user = userRepository.save(user);

        customerRepository.save(Customer.builder()
                .user(user)
                .documentType(blankToNull(form.documentNumber()) != null ? "DNI" : null)
                .documentNumber(blankToNull(form.documentNumber()))
                .createdAt(LocalDateTime.now())
                .build());

        return user;
    }

    private static String blankToNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
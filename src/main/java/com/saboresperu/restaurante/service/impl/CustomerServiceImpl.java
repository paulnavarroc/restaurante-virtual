package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.CustomerProfileFormDto;
import com.saboresperu.restaurante.entity.Customer;
import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.CustomerRepository;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    /*
    @Override
    @Transactional(readOnly = true)
    public Customer requireByUserEmail(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        return customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene perfil de cliente"));
    }

     */

    @Override
    @Transactional(readOnly = true)
    public Customer requireByUserEmail(String email) {
        return customerRepository.findByUserEmailWithUser(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileFormDto getProfileForm(Integer customerId) {
        Customer c = customerRepository.findByIdWithUser(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        User u = c.getUser();
        return new CustomerProfileFormDto(
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                u.getPhone(),
                c.getDocumentType(),
                c.getDocumentNumber(),
                null,
                null
        );
    }

    @Override
    @Transactional
    public void updateProfile(Integer customerId, CustomerProfileFormDto form) {
        Customer c = customerRepository.findByIdWithUser(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        User u = c.getUser();

        String email = form.email().trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(other -> {
            if (!other.getId().equals(u.getId())) {
                throw new IllegalArgumentException("El correo ya está en uso");
            }
        });

        u.setFirstName(form.firstName().trim());
        u.setLastName(form.lastName().trim());
        u.setEmail(email);
        u.setPhone(blankToNull(form.phone()));

        if (form.newPassword() != null && !form.newPassword().isBlank()) {
            if (!form.newPassword().equals(form.confirmPassword())) {
                throw new IllegalArgumentException("Las contraseñas no coinciden");
            }
            u.setPasswordHash(passwordEncoder.encode(form.newPassword()));
        }

        c.setDocumentType(blankToNull(form.documentType()));
        c.setDocumentNumber(blankToNull(form.documentNumber()));

        userRepository.save(u);
        customerRepository.save(c);
    }


    private static String blankToNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
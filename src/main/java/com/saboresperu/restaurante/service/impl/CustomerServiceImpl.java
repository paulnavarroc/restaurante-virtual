package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.entity.Customer;
import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.CustomerRepository;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public Customer requireByUserEmail(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        return customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene perfil de cliente"));
    }
}
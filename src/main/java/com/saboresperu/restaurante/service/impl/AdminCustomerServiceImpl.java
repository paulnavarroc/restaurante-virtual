package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.*;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.CustomerAddressRepository;
import com.saboresperu.restaurante.repository.CustomerRepository;
import com.saboresperu.restaurante.repository.OrderRepository;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.AdminCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCustomerServiceImpl implements AdminCustomerService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAdminRowDto> search(String query) {
        List<Customer> list = (query == null || query.isBlank())
                ? customerRepository.findAllWithUser()
                : customerRepository.search(query.trim());

        return list.stream().map(c -> {
            User u = c.getUser();
            long orders = orderRepository.countByCustomerId(c.getId());
            return new CustomerAdminRowDto(
                    c.getId(),
                    u.getId(),
                    (u.getFirstName() + " " + u.getLastName()).trim(),
                    u.getEmail(),
                    u.getPhone(),
                    c.getDocumentType(),
                    c.getDocumentNumber(),
                    Boolean.TRUE.equals(u.getIsActive()),
                    c.getCreatedAt(),
                    orders
            );
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerAdminDetailDto getDetail(Integer customerId) {
        Customer c = customerRepository.findByIdWithUser(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        User u = c.getUser();

        List<CustomerAddressAdminDto> addresses =
                customerAddressRepository
                        .findByCustomerIdAndIsActiveTrueOrderByIsPrimaryDesc(customerId)
                        .stream()
                        .map(a -> new CustomerAddressAdminDto(
                                a.getId(),
                                a.getAlias(),
                                a.getStreet() + (a.getNumber() != null ? " " + a.getNumber() : ""),
                                a.getDistrict() != null ? a.getDistrict().getName() : null,
                                Boolean.TRUE.equals(a.getIsPrimary()),
                                Boolean.TRUE.equals(a.getIsActive())
                        ))
                        .toList();

        // Si district es lazy y falla, usa find con JOIN FETCH de direcciones

        List<AdminOrderRowDto> recentOrders = orderRepository
                .findRecentByCustomerId(customerId, PageRequest.of(0, 10))
                .stream()
                .map(o -> new AdminOrderRowDto(
                        o.getId(),
                        o.getOrderStatus().getCode(),
                        o.getOrderStatus().getName(),
                        (u.getFirstName() + " " + u.getLastName()).trim(),
                        u.getEmail(),
                        o.getBranch().getName(),
                        o.getTotal(),
                        "—",
                        o.getOrderedAt() != null ? o.getOrderedAt().format(FMT) : null
                ))
                .toList();

        return new CustomerAdminDetailDto(
                c.getId(),
                u.getId(),
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                u.getPhone(),
                c.getDocumentType(),
                c.getDocumentNumber(),
                Boolean.TRUE.equals(u.getIsActive()),
                c.getCreatedAt(),
                u.getLastAccessAt(),
                addresses,
                recentOrders
        );
    }

    @Override
    @Transactional
    public void setUserActive(Integer customerId, boolean active) {
        Customer c = customerRepository.findByIdWithUser(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        User u = c.getUser();
        u.setIsActive(active);
        userRepository.save(u);
    }
}
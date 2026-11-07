package com.saboresperu.restaurante.dto;

import java.time.LocalDateTime;

public record CustomerAdminDetailDto(
        Integer id,
        Integer userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String documentType,
        String documentNumber,
        boolean userActive,
        LocalDateTime registeredAt,
        LocalDateTime lastAccessAt,
        java.util.List<CustomerAddressAdminDto> addresses,
        java.util.List<AdminOrderRowDto> recentOrders
) {}
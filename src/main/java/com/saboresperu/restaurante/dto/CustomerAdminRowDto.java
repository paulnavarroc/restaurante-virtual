package com.saboresperu.restaurante.dto;

import java.time.LocalDateTime;

public record CustomerAdminRowDto(
        Integer id,
        Integer userId,
        String fullName,
        String email,
        String phone,
        String documentType,
        String documentNumber,
        boolean userActive,
        LocalDateTime registeredAt,
        long orderCount
) {}
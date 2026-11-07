package com.saboresperu.restaurante.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserAdminRowDto(
        Integer id,
        String fullName,
        String email,
        String phone,
        boolean active,
        List<String> roleNames,
        LocalDateTime createdAt,
        LocalDateTime lastAccessAt
) {}
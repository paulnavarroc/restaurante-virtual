package com.saboresperu.restaurante.dto;

import java.time.LocalDateTime;

public record AccessLogRowDto(
        Long id,
        String email,
        String userFullName,
        String eventType,
        boolean success,
        String ip,
        String browser,
        String operatingSystem,
        String device,
        String detail,
        LocalDateTime createdAt
) {}
package com.saboresperu.restaurante.dto;

import java.time.LocalDateTime;

public record SecurityAuditRowDto(
        Long id,
        String userEmail,
        String userFullName,
        String action,
        String entityType,
        String entityId,
        String detail,
        LocalDateTime createdAt
) {}
package com.saboresperu.restaurante.dto;

public record AdminOrderHistoryDto(
        String statusName,
        String notes,
        String userName,
        String createdAtLabel
) {}
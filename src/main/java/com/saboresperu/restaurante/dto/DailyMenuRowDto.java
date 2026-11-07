package com.saboresperu.restaurante.dto;

import java.time.LocalDate;

public record DailyMenuRowDto(
        Integer id,
        String branchName,
        LocalDate menuDate,
        String status,
        int itemCount
) {}
package com.saboresperu.restaurante.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyMenuDetailDto(
        Integer id,
        Integer branchId,
        String branchName,
        LocalDate menuDate,
        String status,
        List<DailyMenuItemRowDto> items
) {}
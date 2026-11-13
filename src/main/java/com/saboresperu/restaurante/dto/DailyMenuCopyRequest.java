package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record DailyMenuCopyRequest(
        @NotNull Integer sourceMenuId,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate
) {}
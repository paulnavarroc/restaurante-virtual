package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DailyMenuCreateRequest(
        @NotNull Integer branchId,
        @NotNull LocalDate menuDate
) {}
package com.saboresperu.restaurante.dto;

import java.time.LocalTime;
import java.util.List;

public record BranchShiftDto(
        Integer id,
        String shift,
        String label,
        LocalTime startTime,
        LocalTime endTime,
        boolean active
) {}
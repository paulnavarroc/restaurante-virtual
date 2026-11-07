package com.saboresperu.restaurante.dto;

import java.util.List;

public record DailyMenuItemShiftUpdateRequest(
        List<Integer> branchShiftIds  // vacío = todos los turnos
) {}
package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.entity.BranchShift;
import com.saboresperu.restaurante.repository.BranchShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BranchShiftResolver {

    private final BranchShiftRepository branchShiftRepository;

    public List<BranchShift> activeShifts(Integer branchId) {
        return branchShiftRepository.findByBranchIdAndIsActiveTrue(branchId);
    }

    /** Turno vigente ahora (puede haber solapes; toma el primero que coincida). */
    public Optional<BranchShift> currentShift(Integer branchId, LocalTime time) {
        LocalTime t = time != null ? time : LocalTime.now();
        return activeShifts(branchId).stream()
                .filter(s -> isWithin(s.getStartTime(), s.getEndTime(), t))
                .findFirst();
    }

    public boolean isWithin(LocalTime start, LocalTime end, LocalTime time) {
        if (start == null || end == null || time == null) {
            return true;
        }
        if (start.equals(end)) {
            return true; // 24h
        }
        if (start.isBefore(end) || start.equals(end)) {
            // mismo día: [start, end)
            if (start.isBefore(end)) {
                return !time.isBefore(start) && time.isBefore(end);
            }
        }
        // cruza medianoche: p.ej. 22:00–06:00
        return !time.isBefore(start) || time.isBefore(end);
    }

    public boolean itemVisibleInShift(java.util.Set<BranchShift> itemShifts, BranchShift current) {
        if (itemShifts == null || itemShifts.isEmpty()) {
            return true; // sin restricción
        }
        if (current == null) {
            return true; // fuera de horario configurado: mostrar todo (o false si prefieres ocultar)
        }
        return itemShifts.stream().anyMatch(s -> s.getId().equals(current.getId()));
    }
}
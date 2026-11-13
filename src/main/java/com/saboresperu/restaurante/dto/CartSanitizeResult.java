package com.saboresperu.restaurante.dto;

import java.util.List;

public record CartSanitizeResult(
        CartDto cart,
        List<String> removedNames,
        List<String> adjustedNames
) {
    public boolean hasChanges() {
        return (removedNames != null && !removedNames.isEmpty())
                || (adjustedNames != null && !adjustedNames.isEmpty());
    }
}
package com.saboresperu.restaurante.util;

import com.saboresperu.restaurante.dto.CartSanitizeResult;
import com.saboresperu.restaurante.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelperUtils {
    private final BranchRepository branchRepository;


    public static String shiftLabel(String code) {
        return switch (code) {
            case "BREAKFAST" -> "Desayuno";
            case "LUNCH" -> "Almuerzo";
            case "DINNER" -> "Cena";
            default -> code;
        };
    }

    public Integer resolveDefaultBranchId() {
        return branchRepository.findAll().stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsActive()))
                .map(b -> b.getId())
                .findFirst()
                .orElse(null);
    }

    public static String buildNotice(CartSanitizeResult result) {
        if (result == null || !result.hasChanges()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (result.removedNames() != null && !result.removedNames().isEmpty()) {
            sb.append("Se quitaron del carrito (ya no están en el menú de hoy): ")
                    .append(String.join(", ", result.removedNames()))
                    .append(".");
        }
        if (result.adjustedNames() != null && !result.adjustedNames().isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append("Se ajustó la cantidad por cupo disponible: ")
                    .append(String.join(", ", result.adjustedNames()))
                    .append(".");
        }
        String text = sb.toString().trim();
        return text.isEmpty() ? null : text;
    }
}

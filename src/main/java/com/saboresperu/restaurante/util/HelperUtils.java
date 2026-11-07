package com.saboresperu.restaurante.util;

public class HelperUtils {
    public static String shiftLabel(String code) {
        return switch (code) {
            case "BREAKFAST" -> "Desayuno";
            case "LUNCH" -> "Almuerzo";
            case "DINNER" -> "Cena";
            default -> code;
        };
    }
}

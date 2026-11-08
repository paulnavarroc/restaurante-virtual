package com.saboresperu.restaurante.dto;

public record ProfileUpdateResult(
        boolean emailChanged,
        boolean passwordChanged,
        String firstName,
        String lastName,
        String email
) {
    public boolean requiresReLogin() {
        return emailChanged || passwordChanged;
    }
}
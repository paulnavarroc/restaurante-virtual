package com.saboresperu.restaurante.dto;

import java.util.List;

public record EffectivePermissionPreviewDto(
        String mode,
        List<String> fromRoles,
        List<String> allowOverrides,
        List<String> denyOverrides,
        List<String> effective
) {}
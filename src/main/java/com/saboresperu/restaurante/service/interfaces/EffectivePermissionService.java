package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.MenuItemDto;
import com.saboresperu.restaurante.dto.SessionAuthorizationDto;

import java.util.List;
import java.util.Set;

public interface EffectivePermissionService {
    Set<String> resolvePermissionCodes(Integer userId);
    List<MenuItemDto> resolveAllMenus(Integer userId);
    List<MenuItemDto> resolveMenuByContext(Integer userId, String context);
    SessionAuthorizationDto buildSessionAuthorization(Integer userId);
    long currentSecurityVersion();
}
package com.saboresperu.restaurante.service.interfaces;

public interface SecurityConfigService {
    long currentVersion();
    void bumpSecurityVersion(Integer actorUserId, String reason);
}
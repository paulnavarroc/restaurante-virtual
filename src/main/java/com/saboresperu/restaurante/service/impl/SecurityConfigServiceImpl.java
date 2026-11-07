package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.entity.SecurityAuditLog;
import com.saboresperu.restaurante.entity.SecurityConfig;
import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.SecurityAuditLogRepository;
import com.saboresperu.restaurante.repository.SecurityConfigRepository;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.SecurityConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SecurityConfigServiceImpl implements SecurityConfigService {

    public static final String KEY_VERSION = "SECURITY_VERSION";

    private final SecurityConfigRepository securityConfigRepository;
    private final SecurityAuditLogRepository securityAuditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public long currentVersion() {
        return securityConfigRepository.findByConfigKey(KEY_VERSION)
                .map(c -> {
                    try {
                        return Long.parseLong(c.getConfigValue());
                    } catch (NumberFormatException e) {
                        return 1L;
                    }
                })
                .orElse(1L);
    }

    @Override
    @Transactional
    public void bumpSecurityVersion(Integer actorUserId, String reason) {
        SecurityConfig cfg = securityConfigRepository.findByConfigKey(KEY_VERSION)
                .orElseGet(() -> SecurityConfig.builder()
                        .configKey(KEY_VERSION)
                        .configValue("1")
                        .build());
        long next = currentVersion() + 1;
        cfg.setConfigValue(String.valueOf(next));
        cfg.setUpdatedAt(java.time.LocalDateTime.now());
        securityConfigRepository.save(cfg);

        User actor = actorUserId != null
                ? userRepository.findById(actorUserId).orElse(null) : null;
        securityAuditLogRepository.save(SecurityAuditLog.builder()
                .user(actor)
                .action("SECURITY_VERSION_BUMP")
                .entityType("SecurityConfig")
                .entityId(KEY_VERSION)
                .detail(reason != null ? reason : "Version=" + next)
                .createdAt(java.time.LocalDateTime.now())
                .build());
    }
}
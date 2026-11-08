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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SecurityConfigServiceImpl implements SecurityConfigService {

    private static final int SINGLETON_ID = 1;

    private final SecurityConfigRepository securityConfigRepository;
    private final SecurityAuditLogRepository securityAuditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public long currentVersion() {
        return securityConfigRepository.findById(SINGLETON_ID)
                .map(c -> c.getSecurityVersion() != null
                        ? c.getSecurityVersion().longValue()
                        : 1L)
                .orElse(1L);
    }

    @Override
    @Transactional
    public void bumpSecurityVersion(Integer actorUserId, String reason) {
        SecurityConfig cfg = securityConfigRepository
                .findByIdForUpdate(SINGLETON_ID)
                .orElseGet(() -> SecurityConfig.builder()
                        .id(SINGLETON_ID)
                        .securityVersion(1)
                        .updatedAt(LocalDateTime.now())
                        .build());

        int next = (cfg.getSecurityVersion() != null ? cfg.getSecurityVersion() : 1) + 1;
        cfg.setSecurityVersion(next);
        cfg.setUpdatedAt(LocalDateTime.now());
        securityConfigRepository.save(cfg);

        User actor = actorUserId != null
                ? userRepository.findById(actorUserId).orElse(null)
                : null;

        securityAuditLogRepository.save(SecurityAuditLog.builder()
                .user(actor)
                .action("SECURITY_VERSION_BUMP")
                .entityType("SecurityConfig")
                .entityId(String.valueOf(SINGLETON_ID))
                .detail(reason != null ? reason : ("Version=" + next))
                .createdAt(LocalDateTime.now())
                .build());
    }
}
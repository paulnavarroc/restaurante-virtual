package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.AccessLogRowDto;
import com.saboresperu.restaurante.dto.SecurityAuditRowDto;
import com.saboresperu.restaurante.entity.SecurityAuditLog;
import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.entity.UserAccessLog;
import com.saboresperu.restaurante.repository.SecurityAuditLogRepository;
import com.saboresperu.restaurante.repository.UserAccessLogRepository;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.AccessAuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessAuditServiceImpl implements AccessAuditService {

    private final UserAccessLogRepository userAccessLogRepository;
    private final SecurityAuditLogRepository securityAuditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void loginSuccess(Integer userId, String email, HttpServletRequest request, String sessionId) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        ClientInfo info = ClientInfo.from(request);

        if (user != null) {
            user.setLastAccessAt(LocalDateTime.now());
            user.setLastIp(info.ip());
            user.setLastUserAgent(trim(info.userAgent(), 500));
            userRepository.save(user);
        }

        userAccessLogRepository.save(UserAccessLog.builder()
                .user(user)
                .emailAttempted(email)
                .eventType("LOGIN_SUCCESS")
                .isSuccess(true)
                .ip(info.ip())
                .userAgent(trim(info.userAgent(), 500))
                .browser(info.browser())
                .operatingSystem(info.os())
                .device(info.device())
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional
    public void loginFailure(String emailAttempted, HttpServletRequest request, String detail) {
        ClientInfo info = ClientInfo.from(request);
        userAccessLogRepository.save(UserAccessLog.builder()
                .emailAttempted(emailAttempted)
                .eventType("LOGIN_FAILURE")
                .isSuccess(false)
                .ip(info.ip())
                .userAgent(trim(info.userAgent(), 500))
                .browser(info.browser())
                .operatingSystem(info.os())
                .device(info.device())
                .detail(trim(detail, 300))
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional
    public void logout(Integer userId, String email, HttpServletRequest request, String sessionId) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        ClientInfo info = ClientInfo.from(request);
        userAccessLogRepository.save(UserAccessLog.builder()
                .user(user)
                .emailAttempted(email)
                .eventType("LOGOUT")
                .isSuccess(true)
                .ip(info.ip())
                .userAgent(trim(info.userAgent(), 500))
                .browser(info.browser())
                .operatingSystem(info.os())
                .device(info.device())
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccessLogRowDto> listAccessLogs(int limit) {
        return userAccessLogRepository.findRecientes(PageRequest.of(0, limit)).stream()
                .map(a -> {
                    User u = a.getUser();
                    String name = u != null
                            ? (u.getFirstName() + " " + u.getLastName()).trim()
                            : null;
                    return new AccessLogRowDto(
                            a.getId(),
                            a.getEmailAttempted(),
                            name,
                            a.getEventType(),
                            Boolean.TRUE.equals(a.getIsSuccess()),
                            a.getIp(),
                            a.getBrowser(),
                            a.getOperatingSystem(),
                            a.getDevice(),
                            a.getDetail(),
                            a.getCreatedAt()
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecurityAuditRowDto> listSecurityAudits(int limit) {
        return securityAuditLogRepository
                .findTopNWithUser(PageRequest.of(0, limit))
                .stream()
                .map(s -> {
                    User u = s.getUser();
                    return new SecurityAuditRowDto(
                            s.getId(),
                            u != null ? u.getEmail() : null,
                            u != null ? (u.getFirstName() + " " + u.getLastName()).trim() : null,
                            s.getAction(),
                            s.getEntityType(),
                            s.getEntityId(),
                            s.getDetail(),
                            s.getCreatedAt()
                    );
                })
                .toList();
    }

    private static String trim(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    /** Parser simple de User-Agent (suficiente para auditoría). */
    private record ClientInfo(String ip, String userAgent, String browser, String os, String device) {
        static ClientInfo from(HttpServletRequest request) {
            String ua = request.getHeader("User-Agent");
            String ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isBlank()) {
                ip = ip.split(",")[0].trim();
            } else {
                ip = request.getRemoteAddr();
            }
            String browser = "Other";
            String os = "Other";
            String device = "Desktop";
            if (ua != null) {
                String u = ua.toLowerCase();
                if (u.contains("edg/")) browser = "Edge";
                else if (u.contains("chrome/")) browser = "Chrome";
                else if (u.contains("firefox/")) browser = "Firefox";
                else if (u.contains("safari/") && !u.contains("chrome")) browser = "Safari";

                if (u.contains("windows")) os = "Windows";
                else if (u.contains("mac os")) os = "macOS";
                else if (u.contains("android")) os = "Android";
                else if (u.contains("iphone") || u.contains("ipad")) os = "iOS";
                else if (u.contains("linux")) os = "Linux";

                if (u.contains("mobile") || u.contains("android") || u.contains("iphone")) {
                    device = "Mobile";
                } else if (u.contains("ipad") || u.contains("tablet")) {
                    device = "Tablet";
                }
            }
            return new ClientInfo(ip, ua, browser, os, device);
        }
    }
}
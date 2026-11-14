package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.AccessLogRowDto;
import com.saboresperu.restaurante.dto.SecurityAuditRowDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccessAuditService {
    void loginSuccess(Integer userId, String email, HttpServletRequest request, String sessionId);
    void loginFailure(String emailAttempted, HttpServletRequest request, String detail);
    void logout(Integer userId, String email, HttpServletRequest request, String sessionId);

    List<AccessLogRowDto> listAccessLogs(int limit);
    List<SecurityAuditRowDto> listSecurityAudits(int limit);
    Page<AccessLogRowDto> pageAccessLogs(Pageable pageable);
    Page<SecurityAuditRowDto> pageSecurityAudits(Pageable pageable);
}
package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.AccessLogRowDto;
import com.saboresperu.restaurante.dto.SecurityAuditRowDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AccessAuditService {
    void loginSuccess(Integer userId, String email, HttpServletRequest request, String sessionId);
    void loginFailure(String emailAttempted, HttpServletRequest request, String detail);
    void logout(Integer userId, String email, HttpServletRequest request, String sessionId);

    List<AccessLogRowDto> listAccessLogs(int limit);
    List<SecurityAuditRowDto> listSecurityAudits(int limit);
}
package com.saboresperu.restaurante.security;

import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.AccessAuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LogoutAuditHandler extends SimpleUrlLogoutSuccessHandler {

    private final AccessAuditService accessAuditService;
    private final UserRepository userRepository;

    public LogoutAuditHandler(AccessAuditService accessAuditService, UserRepository userRepository) {
        this.accessAuditService = accessAuditService;
        this.userRepository = userRepository;
        setDefaultTargetUrl("/login?logout");
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication != null) {
            String email = authentication.getName();
            Integer userId = userRepository.findByEmail(email).map(User::getId).orElse(null);
            String sessionId = request.getSession(false) != null ? request.getSession(false).getId() : null;
            accessAuditService.logout(userId, email, request, sessionId);
        }
        super.onLogoutSuccess(request, response, authentication);
    }
}
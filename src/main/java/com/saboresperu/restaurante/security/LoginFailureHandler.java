package com.saboresperu.restaurante.security;

import com.saboresperu.restaurante.service.interfaces.AccessAuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final AccessAuditService accessAuditService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        String username = request.getParameter("username");
        accessAuditService.loginFailure(username, request, exception.getMessage());

        String msg = URLEncoder.encode("Correo o contraseña incorrectos", StandardCharsets.UTF_8);
        getRedirectStrategy().sendRedirect(request, response, "/login?error&message=" + msg);
    }
}
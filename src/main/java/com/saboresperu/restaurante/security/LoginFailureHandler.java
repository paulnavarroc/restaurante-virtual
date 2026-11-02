package com.saboresperu.restaurante.security;

import com.saboresperu.restaurante.service.interfaces.AuditoriaAccesoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final AuditoriaAccesoService auditoriaAccesoService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        String correo = request.getParameter("username");
        auditoriaAccesoService.registrarLoginFail(
                correo, request, exception.getMessage());
        response.sendRedirect(request.getContextPath() + "/login?error");
    }
}
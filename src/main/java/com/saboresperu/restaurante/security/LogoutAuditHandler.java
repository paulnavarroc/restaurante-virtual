package com.saboresperu.restaurante.security;

import com.saboresperu.restaurante.repository.UsuarioRepository;
import com.saboresperu.restaurante.service.interfaces.AuditoriaAccesoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LogoutAuditHandler implements LogoutSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaAccesoService auditoriaAccesoService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication)
            throws IOException, ServletException {
        if (authentication != null) {
            usuarioRepository.findByCorreo(authentication.getName())
                    .ifPresent(u -> auditoriaAccesoService.registrarLogout(u, request));
        }
        response.sendRedirect(request.getContextPath() + "/login?logout");
    }
}
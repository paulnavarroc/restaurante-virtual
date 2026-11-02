package com.saboresperu.restaurante.security;

import com.saboresperu.restaurante.entity.Usuario;
import com.saboresperu.restaurante.repository.UsuarioRepository;
import com.saboresperu.restaurante.service.interfaces.AuditoriaAccesoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class RoleBasedAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaAccesoService auditoriaAccesoService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String correo = authentication.getName();
        usuarioRepository.findByCorreo(correo).ifPresent(u ->
                auditoriaAccesoService.registrarLoginOk(u, request));

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        if (hasRole(authorities, "ROLE_ADMINISTRADOR") || hasRole(authorities, "ROLE_OPERACIONES")) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }
        if (hasRole(authorities, "ROLE_CHEF")) {
            response.sendRedirect(request.getContextPath() + "/cocina/pedidos");
            return;
        }
        if (hasRole(authorities, "ROLE_REPARTIDOR")) {
            response.sendRedirect(request.getContextPath() + "/repartidor/entregas");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/menu");
    }

    private boolean hasRole(Collection<? extends GrantedAuthority> authorities, String role) {
        return authorities.stream().anyMatch(a -> role.equals(a.getAuthority()));
    }
}
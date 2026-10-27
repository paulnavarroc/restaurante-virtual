package com.saboresperu.restaurante.util;

import com.saboresperu.restaurante.entity.Usuario;
import com.saboresperu.restaurante.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UsuarioRepository usuarioRepository;

    public Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String correo = auth.getName();
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    public Integer getUsuarioIdActual() {
        Usuario usuario = getUsuarioActual();
        return usuario != null ? usuario.getId() : null;
    }
}
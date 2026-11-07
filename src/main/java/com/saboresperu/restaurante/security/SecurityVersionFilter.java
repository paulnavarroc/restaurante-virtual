package com.saboresperu.restaurante.security;

import com.saboresperu.restaurante.domain.SecuritySessionKeys;
import com.saboresperu.restaurante.service.interfaces.EffectivePermissionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityVersionFilter extends OncePerRequestFilter {

    private final EffectivePermissionService effectivePermissionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object v = session.getAttribute(SecuritySessionKeys.SECURITY_VERSION);
                long sessionVersion = (v instanceof Number n) ? n.longValue() : -1L;
                if (sessionVersion >= 0) {
                    long current = effectivePermissionService.currentSecurityVersion();
                    if (sessionVersion != current) {
                        session.invalidate();
                        SecurityContextHolder.clearContext();
                        response.sendRedirect(request.getContextPath() + "/login?securityUpdated");
                        return;
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
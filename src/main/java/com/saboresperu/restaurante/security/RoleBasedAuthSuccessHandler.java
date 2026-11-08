package com.saboresperu.restaurante.security;

import com.saboresperu.restaurante.domain.SecuritySessionKeys;
import com.saboresperu.restaurante.domain.SessionUserKeys;
import com.saboresperu.restaurante.dto.SessionAuthorizationDto;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.impl.AccessAuditServiceImpl;
import com.saboresperu.restaurante.service.interfaces.EffectivePermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

    private final UserRepository userRepository;
    private final EffectivePermissionService effectivePermissionService;
    private final AccessAuditServiceImpl accessAuditService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        userRepository.findByEmail(authentication.getName()).ifPresent(user -> {
            SessionAuthorizationDto authz = effectivePermissionService.buildSessionAuthorization(user.getId());

            String displayName = (user.getFirstName() + " " + user.getLastName()).trim();
            String initials = "";
            if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
                initials += user.getFirstName().substring(0, 1).toUpperCase();
            }
            if (user.getLastName() != null && !user.getLastName().isBlank()) {
                initials += user.getLastName().substring(0, 1).toUpperCase();
            }
            if (initials.isEmpty()) {
                initials = "?";
            }

            HttpSession session = request.getSession(true);
            session.setAttribute(SessionUserKeys.DISPLAY_NAME, displayName);
            session.setAttribute(SessionUserKeys.INITIALS, initials);
            session.setAttribute(SessionUserKeys.EMAIL, user.getEmail());
            session.setAttribute(SecuritySessionKeys.SECURITY_VERSION, authz.securityVersion());
            session.setAttribute(SecuritySessionKeys.EFFECTIVE_PERMISSIONS, authz.permissionCodes());
            session.setAttribute(SecuritySessionKeys.MENU_ITEMS, authz.menuItems());

            String sessionId = request.getSession(true).getId();
            accessAuditService.loginSuccess(user.getId(), user.getEmail(), request, sessionId);
        });

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (has(authorities, "ROLE_ADMINISTRATOR") || has(authorities, "ROLE_MANAGER") || has(authorities, "ROLE_OPERATIONS")) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }
        if (has(authorities, "ROLE_CHEF")) {
            response.sendRedirect(request.getContextPath() + "/kitchen/orders");
            return;
        }
        if (has(authorities, "ROLE_COURIER")) {
            response.sendRedirect(request.getContextPath() + "/courier/deliveries");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/menu");
    }

    private boolean has(Collection<? extends GrantedAuthority> authorities, String role) {
        return authorities.stream().anyMatch(a -> role.equals(a.getAuthority()));
    }
}
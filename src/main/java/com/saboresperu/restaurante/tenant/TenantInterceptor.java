package com.saboresperu.restaurante.tenant;

import com.saboresperu.restaurante.config.TenantProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantProperties tenantProperties;

    public TenantInterceptor(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = resolveTenant(request);
        TenantContext.setTenantId(tenantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear(); // Muy importante
    }

    private String resolveTenant(HttpServletRequest request) {
        // 1. Por Header (recomendado para APIs)
        String tenantHeader = request.getHeader("X-Tenant-ID");
        if (tenantHeader != null && tenantProperties.getConfigs().containsKey(tenantHeader)) {
            return tenantHeader;
        }

        // 2. Por subdominio (lima.saboresperu.com)
        String host = request.getServerName();
        if (host != null && host.contains(".")) {
            String subdomain = host.split("\\.")[0];
            if (tenantProperties.getConfigs().containsKey(subdomain)) {
                return subdomain;
            }
        }

        // 3. Tenant por defecto
        return tenantProperties.getDefaultTenant();
    }
}
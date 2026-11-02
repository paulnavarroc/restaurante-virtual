package com.saboresperu.restaurante.tenant;

import com.saboresperu.restaurante.config.TenantProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Profile("multi-tenant")
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantProperties tenantProperties;

    public TenantInterceptor(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        TenantContext.setTenantId(resolveTenant(request));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
    }

    private String resolveTenant(HttpServletRequest request) {
        String tenantHeader = request.getHeader("X-Tenant-ID");
        if (tenantHeader != null
                && tenantProperties.getConfigs() != null
                && tenantProperties.getConfigs().containsKey(tenantHeader)) {
            return tenantHeader;
        }

        String host = request.getServerName();
        if (host != null && host.contains(".")
                && tenantProperties.getConfigs() != null) {
            String subdomain = host.split("\\.")[0];
            if (tenantProperties.getConfigs().containsKey(subdomain)) {
                return subdomain;
            }
        }
        return tenantProperties.getDefaultTenant();
    }
}
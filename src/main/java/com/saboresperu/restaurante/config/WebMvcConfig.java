package com.saboresperu.restaurante.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración MVC base.
 * El TenantInterceptor solo se registra con profile multi-tenant
 * (ver MultiTenantWebMvcConfig si lo usas).
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // Vacío a propósito: no inyectar TenantInterceptor aquí.
}
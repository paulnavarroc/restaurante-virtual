package com.saboresperu.restaurante.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "tenants")
public class TenantProperties {

    private String defaultTenant = "lima";
    private Map<String, DataSourceConfig> configs = new LinkedHashMap<>();

    @Data
    public static class DataSourceConfig {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private int maximumPoolSize = 10;
    }
}
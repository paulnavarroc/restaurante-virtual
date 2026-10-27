package com.saboresperu.restaurante.config;

import com.saboresperu.restaurante.tenant.TenantRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(TenantProperties.class)
public class MultiTenantDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(MultiTenantDataSourceConfig.class);

    private final TenantProperties tenantProperties;

    public MultiTenantDataSourceConfig(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();

        if (tenantProperties.getConfigs() == null || tenantProperties.getConfigs().isEmpty()) {
            throw new IllegalStateException(
                    "No hay tenants configurados. Revisa application.yml → tenants.configs"
            );
        }

        tenantProperties.getConfigs().forEach((tenantId, config) -> {
            log.info("Configurando DataSource para tenant: {}", tenantId);

            HikariDataSource ds = DataSourceBuilder.create()
                    .type(HikariDataSource.class)
                    .url(config.getUrl())
                    .username(config.getUsername())
                    .password(config.getPassword())
                    .driverClassName(config.getDriverClassName())
                    .build();

            ds.setPoolName("HikariPool-" + tenantId);
            ds.setMaximumPoolSize(config.getMaximumPoolSize() > 0 ? config.getMaximumPoolSize() : 10);
            ds.setInitializationFailTimeout(-1);

            targetDataSources.put(tenantId, ds);
        });

        String defaultTenant = tenantProperties.getDefaultTenant();
        if (defaultTenant == null || defaultTenant.isBlank()) {
            defaultTenant = "lima";
        }

        DataSource defaultDs = (DataSource) targetDataSources.get(defaultTenant);
        if (defaultDs == null) {
            // Si la key no coincide, usa el primero disponible
            defaultTenant = tenantProperties.getConfigs().keySet().iterator().next();
            defaultDs = (DataSource) targetDataSources.get(defaultTenant);
            log.warn("Default tenant no encontrado. Usando: {}", defaultTenant);
        }

        log.info("Tenant por defecto: {}", defaultTenant);

        TenantRoutingDataSource routing = new TenantRoutingDataSource();
        routing.setDefaultTenant(defaultTenant);
        routing.setTargetDataSources(targetDataSources);
        routing.setDefaultTargetDataSource(defaultDs);
        routing.afterPropertiesSet();

        return routing;
    }
}
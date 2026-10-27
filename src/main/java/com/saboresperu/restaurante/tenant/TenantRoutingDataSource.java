package com.saboresperu.restaurante.tenant;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private String defaultTenant = "lima";

    public void setDefaultTenant(String defaultTenant) {
        if (defaultTenant != null && !defaultTenant.isBlank()) {
            this.defaultTenant = defaultTenant;
        }
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            return defaultTenant;
        }
        return tenantId;
    }
}
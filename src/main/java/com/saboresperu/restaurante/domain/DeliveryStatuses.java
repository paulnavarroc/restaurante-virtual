package com.saboresperu.restaurante.domain;

public final class DeliveryStatuses {
    public static final String PENDING = "PENDING";
    public static final String ASSIGNED = "ASSIGNED";
    public static final String ON_ROUTE = "ON_ROUTE";
    public static final String DELIVERED = "DELIVERED";
    public static final String FAILED = "FAILED";
    private DeliveryStatuses() {}
}
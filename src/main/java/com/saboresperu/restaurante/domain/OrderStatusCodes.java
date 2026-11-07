package com.saboresperu.restaurante.domain;

public final class OrderStatusCodes {
    public static final String PENDING = "PENDING";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String IN_PREPARATION = "IN_PREPARATION";
    public static final String READY = "READY";
    public static final String OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";
    private OrderStatusCodes() {}
}
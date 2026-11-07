package com.saboresperu.restaurante.domain;

public final class PaymentStatuses {
    public static final String PENDING = "PENDING";
    public static final String AUTHORIZED = "AUTHORIZED";
    public static final String PAID = "PAID";
    public static final String FAILED = "FAILED";
    public static final String REFUNDED = "REFUNDED";
    private PaymentStatuses() {}
}
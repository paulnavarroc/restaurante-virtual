package com.saboresperu.restaurante.domain;

public final class EstadosEntrega {
    public static final String PENDIENTE_ASIGNACION = "PENDIENTE_ASIGNACION";
    public static final String ASIGNADA = "ASIGNADA";
    public static final String EN_CAMINO = "EN_CAMINO";
    public static final String ENTREGADA = "ENTREGADA";
    public static final String FALLIDA = "FALLIDA";
    private EstadosEntrega() {}
}
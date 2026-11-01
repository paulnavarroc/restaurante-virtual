package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.entity.Sede;
import java.math.BigDecimal;

public interface DeliveryService {
    BigDecimal calcularCosto(Sede sede, double distanciaKm, BigDecimal subtotalPedido);
}
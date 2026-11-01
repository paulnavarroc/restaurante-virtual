package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.entity.DireccionCliente;
import com.saboresperu.restaurante.entity.Sede;

import java.math.BigDecimal;
import java.util.Optional;

public interface GeoService {
    double distanciaKm(double lat1, double lng1, double lat2, double lng2);
    Optional<Sede> resolverSedeParaDireccion(DireccionCliente direccion);
}
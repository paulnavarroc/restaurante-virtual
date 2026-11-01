package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.entity.DireccionCliente;
import com.saboresperu.restaurante.entity.Sede;
import com.saboresperu.restaurante.repository.SedeRepository;
import com.saboresperu.restaurante.service.interfaces.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GeoServiceImpl implements GeoService {

    private final SedeRepository sedeRepository;

    @Override
    public double distanciaKm(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sede> resolverSedeParaDireccion(DireccionCliente direccion) {
        BigDecimal lat = direccion.getLat() != null
                ? direccion.getLat()
                : (direccion.getDistrito() != null ? direccion.getDistrito().getLatCentro() : null);
        BigDecimal lng = direccion.getLng() != null
                ? direccion.getLng()
                : (direccion.getDistrito() != null ? direccion.getDistrito().getLngCentro() : null);

        if (lat == null || lng == null) {
            return Optional.empty();
        }

        double latC = lat.doubleValue();
        double lngC = lng.doubleValue();

        return sedeRepository.findByActivaTrue().stream()
                .map(s -> {
                    double d = distanciaKm(latC, lngC,
                            s.getLat().doubleValue(), s.getLng().doubleValue());
                    return new Object() {
                        Sede sede = s;
                        double dist = d;
                    };
                })
                .filter(x -> x.dist <= x.sede.getRadioCoberturaKm().doubleValue())
                .min(Comparator.comparingDouble(x -> x.dist))
                .map(x -> x.sede);
    }
}
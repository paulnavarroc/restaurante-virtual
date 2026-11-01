package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.entity.ConfiguracionSede;
import com.saboresperu.restaurante.entity.Sede;
import com.saboresperu.restaurante.entity.TarifaDelivery;
import com.saboresperu.restaurante.repository.ConfiguracionSedeRepository;
import com.saboresperu.restaurante.repository.TarifaDeliveryRepository;
import com.saboresperu.restaurante.service.interfaces.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final ConfiguracionSedeRepository configuracionSedeRepository;
    private final TarifaDeliveryRepository tarifaDeliveryRepository;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularCosto(Sede sede, double distanciaKm, BigDecimal subtotalPedido) {
        ConfiguracionSede cfg = configuracionSedeRepository.findById(sede.getId()).orElse(null);

        if (cfg != null) {
            if (cfg.getDeliveryGratisDesdeMonto() != null
                    && subtotalPedido.compareTo(cfg.getDeliveryGratisDesdeMonto()) >= 0) {
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
            if (cfg.getDeliveryGratisHastaKm() != null
                    && distanciaKm <= cfg.getDeliveryGratisHastaKm().doubleValue()) {
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
        }

        List<TarifaDelivery> tarifas = tarifaDeliveryRepository.findBySedeIdOrderByKmDesdeAsc(sede.getId());
        BigDecimal dist = BigDecimal.valueOf(distanciaKm);

        for (TarifaDelivery t : tarifas) {
            if (dist.compareTo(t.getKmDesde()) >= 0 && dist.compareTo(t.getKmHasta()) < 0) {
                return t.getCosto().setScale(2, RoundingMode.HALF_UP);
            }
            // último tramo inclusivo superior
            if (dist.compareTo(t.getKmDesde()) >= 0 && dist.compareTo(t.getKmHasta()) <= 0
                    && t == tarifas.get(tarifas.size() - 1)) {
                return t.getCosto().setScale(2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal minimo = cfg != null && cfg.getCostoDeliveryMinimo() != null
                ? cfg.getCostoDeliveryMinimo()
                : new BigDecimal("8.00");
        return minimo.setScale(2, RoundingMode.HALF_UP);
    }
}
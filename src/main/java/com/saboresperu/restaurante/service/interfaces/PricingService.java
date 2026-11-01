package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.ResultadoPricingDTO;
import com.saboresperu.restaurante.entity.Sede;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * productoCantidades: productoId -> cantidad
 * preciosLista: productoId -> precio lista unitario (ya del menú del día)
 */
public interface PricingService {

    ResultadoPricingDTO calcular(
            Integer sedeId,
            Integer clienteId,
            Map<Integer, Integer> productoCantidades,
            Map<Integer, BigDecimal> preciosLista,
            Map<Integer, Integer> productoToMenuItemId,
            BigDecimal deliveryBase,
            String codigoCupon
    );
}
package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.RecetaDTO;
import com.saboresperu.restaurante.dto.RecetaFormDTO;

public interface RecetaService {
    RecetaDTO obtenerPorProducto(Integer productoId);
    void guardar(Integer productoId, RecetaFormDTO form);
}
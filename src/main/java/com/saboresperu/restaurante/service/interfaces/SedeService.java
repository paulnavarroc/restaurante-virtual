package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.SedeDTO;
import java.util.List;

public interface SedeService {
    List<SedeDTO> listarActivas();
    SedeDTO obtenerPorId(Integer id);
}
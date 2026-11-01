package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.InsumoDTO;
import com.saboresperu.restaurante.dto.InsumoFormDTO;
import java.util.List;

public interface InsumoService {
    List<InsumoDTO> listarTodos();
    InsumoFormDTO obtenerForm(Integer id);
    Integer crear(InsumoFormDTO form);
    void actualizar(Integer id, InsumoFormDTO form);
}
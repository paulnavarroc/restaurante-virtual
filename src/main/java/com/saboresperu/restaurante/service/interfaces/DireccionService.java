package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.DireccionDTO;
import java.util.List;

public interface DireccionService {

    List<DireccionDTO> listarPorCliente(Integer clienteId);

    DireccionDTO obtenerPorId(Integer direccionId, Integer clienteId);

    DireccionDTO crear(Integer clienteId, DireccionDTO dto);

    DireccionDTO actualizar(Integer direccionId, Integer clienteId, DireccionDTO dto);

    void eliminar(Integer direccionId, Integer clienteId);

    void marcarComoPrincipal(Integer direccionId, Integer clienteId);
}
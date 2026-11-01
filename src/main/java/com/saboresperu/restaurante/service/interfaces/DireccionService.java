package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.DireccionDTO;
import com.saboresperu.restaurante.dto.DireccionFormDTO;

import java.util.List;

public interface DireccionService {
    List<DireccionDTO> listarPorCliente(Integer clienteId);
    void crear(Integer clienteId, DireccionFormDTO form);
    void marcarPrincipal(Integer direccionId, Integer clienteId);
    void eliminar(Integer direccionId, Integer clienteId);
}
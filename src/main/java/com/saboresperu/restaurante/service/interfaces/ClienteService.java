package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.entity.Cliente;

public interface ClienteService {
    Cliente obtenerPorUsuarioId(Integer usuarioId);
    Cliente obtenerPorId(Integer clienteId);
}
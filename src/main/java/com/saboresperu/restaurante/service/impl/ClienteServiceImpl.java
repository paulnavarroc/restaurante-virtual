package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.entity.Cliente;
import com.saboresperu.restaurante.repository.ClienteRepository;
import com.saboresperu.restaurante.service.interfaces.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    public Cliente obtenerPorUsuarioId(Integer usuarioId) {
        return clienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    @Override
    public Cliente obtenerPorId(Integer clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }
}
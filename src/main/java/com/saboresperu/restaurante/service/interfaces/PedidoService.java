package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.*;

import java.util.List;

public interface PedidoService {

    PedidoResponseDTO crearPedido(Integer clienteId, CrearPedidoRequestDTO dto);

    PedidoResponseDTO obtenerPorId(Integer pedidoId, Integer clienteId);

    List<PedidoResponseDTO> listarPorCliente(Integer clienteId);

    List<PedidoResponseDTO> listarPorEstadosYSede(Integer sedeId, List<String> estados);

    PedidoResponseDTO cambiarEstado(Integer pedidoId, CambiarEstadoPedidoDTO dto, Integer usuarioId);
}
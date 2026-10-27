package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.CambiarEstadoPedidoDTO;
import com.saboresperu.restaurante.dto.CrearPedidoRequestDTO;
import com.saboresperu.restaurante.dto.PedidoResponseDTO;
import java.util.List;

public interface PedidoService {
    PedidoResponseDTO crearPedido(Integer clienteId, CrearPedidoRequestDTO dto);
    PedidoResponseDTO obtenerPorId(Integer pedidoId, Integer clienteId);
    List<PedidoResponseDTO> listarPorCliente(Integer clienteId);
    List<PedidoResponseDTO> listarPorEstados(List<String> estados);
    PedidoResponseDTO cambiarEstado(Integer pedidoId, CambiarEstadoPedidoDTO dto, Integer usuarioId);
}
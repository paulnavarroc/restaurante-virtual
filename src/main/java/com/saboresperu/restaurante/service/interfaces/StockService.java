package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.EntradaStockRequestDTO;
import com.saboresperu.restaurante.dto.StockSedeDTO;
import com.saboresperu.restaurante.entity.Pedido;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StockService {

    void registrarEntrada(EntradaStockRequestDTO request, Integer usuarioId);

    /**
     * Calcula insumos requeridos (productoId → cantidad platos) y reserva en la sede.
     * Lanza BusinessException si no hay stock.
     */
    void reservarParaPedido(Pedido pedido, Map<Integer, Integer> productoCantidades);

    void consumirReservas(Integer pedidoId);

    void liberarReservas(Integer pedidoId);

    List<StockSedeDTO> listarStockPorSede(Integer sedeId);
}
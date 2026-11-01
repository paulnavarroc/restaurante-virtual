package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.ReservaStockPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaStockPedidoRepository extends JpaRepository<ReservaStockPedido, Integer> {
    List<ReservaStockPedido> findByPedidoIdAndEstado(Integer pedidoId, String estado);
}

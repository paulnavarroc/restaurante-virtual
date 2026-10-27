package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.HistorialPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialPedidoRepository extends JpaRepository<HistorialPedido, Integer> {
    List<HistorialPedido> findByPedidoIdOrderByFechaCambioAsc(Integer pedidoId);
}
package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EntregaRepository extends JpaRepository<Entrega, Integer> {
    Optional<Entrega> findByPedidoId(Integer pedidoId);
    List<Entrega> findByRepartidorIdAndEstadoIn(Integer repartidorId, List<String> estados);
    List<Entrega> findByEstado(String estado);
}
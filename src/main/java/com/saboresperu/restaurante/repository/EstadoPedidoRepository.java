package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EstadoPedidoRepository extends JpaRepository<EstadoPedido, Integer> {
    Optional<EstadoPedido> findByNombre(String nombre);
}
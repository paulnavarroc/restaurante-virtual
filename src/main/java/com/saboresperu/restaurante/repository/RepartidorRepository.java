package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RepartidorRepository extends JpaRepository<Repartidor, Integer> {
    Optional<Repartidor> findByUsuarioId(Integer usuarioId);
    List<Repartidor> findByDisponibleTrueAndActivoTrue();
}
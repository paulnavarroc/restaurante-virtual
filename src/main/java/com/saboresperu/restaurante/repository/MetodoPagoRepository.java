package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {
    Optional<MetodoPago> findByNombre(String nombre);
    List<MetodoPago> findByActivoTrue();
}
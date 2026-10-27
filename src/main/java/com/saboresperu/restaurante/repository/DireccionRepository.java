package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DireccionRepository extends JpaRepository<Direccion, Integer> {
    List<Direccion> findByClienteIdAndActivoTrue(Integer clienteId);
    List<Direccion> findByClienteId(Integer clienteId);
}
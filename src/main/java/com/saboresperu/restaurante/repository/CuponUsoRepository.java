package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.CuponUso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuponUsoRepository extends JpaRepository<CuponUso, Integer> {
    long countByCuponIdAndClienteId(Integer cuponId, Integer clienteId);
}
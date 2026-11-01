package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsumoRepository extends JpaRepository<Insumo, Integer> {
    List<Insumo> findAllByOrderByNombreAsc();
    List<Insumo> findByActivoTrueOrderByNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
}

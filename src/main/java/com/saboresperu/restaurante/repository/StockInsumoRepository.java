package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.StockInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockInsumoRepository extends JpaRepository<StockInsumo, Integer> {
    Optional<StockInsumo> findBySedeIdAndInsumoId(Integer sedeId, Integer insumoId);
    @Query("""
       SELECT s FROM StockInsumo s
       JOIN FETCH s.insumo
       JOIN FETCH s.sede
       WHERE s.sede.id = :sedeId
       ORDER BY s.insumo.nombre
       """)
    List<StockInsumo> findBySedeIdWithInsumo(@Param("sedeId") Integer sedeId);
}

package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PromocionRepository extends JpaRepository<Promocion, Integer> {

    @Query("""
           SELECT p FROM Promocion p
           WHERE p.activa = true
             AND p.requiereCodigo = false
             AND p.fechaHoraInicio <= :ahora
             AND p.fechaHoraFin >= :ahora
             AND (p.sede IS NULL OR p.sede.id = :sedeId)
             AND (p.cupoGlobal IS NULL OR p.usosActuales < p.cupoGlobal)
           ORDER BY p.prioridad ASC
           """)
    List<Promocion> findAutomaticasVigentes(@Param("sedeId") Integer sedeId,
                                            @Param("ahora") LocalDateTime ahora);

    List<Promocion> findAllByOrderByFechaHoraInicioDesc();
}
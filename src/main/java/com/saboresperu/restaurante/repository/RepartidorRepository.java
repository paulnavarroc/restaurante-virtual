package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepartidorRepository extends JpaRepository<Repartidor, Integer> {
    Optional<Repartidor> findByUsuarioId(Integer usuarioId);

    @Query("""
           SELECT r FROM Repartidor r
           JOIN FETCH r.usuario
           JOIN FETCH r.sede
           WHERE r.sede.id = :sedeId
             AND r.activo = true
             AND r.estado = :estado
           """)
    List<Repartidor> findDisponiblesBySede(@Param("sedeId") Integer sedeId,
                                           @Param("estado") String estado);

    List<Repartidor> findBySedeIdAndActivoTrueAndEstado(Integer sedeId, String estado);
}
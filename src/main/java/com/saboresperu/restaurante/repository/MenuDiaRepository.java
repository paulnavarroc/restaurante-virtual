package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.MenuDia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MenuDiaRepository extends JpaRepository<MenuDia, Integer> {
    @Query("""
           SELECT DISTINCT m FROM MenuDia m
           JOIN FETCH m.sede
           LEFT JOIN FETCH m.items i
           LEFT JOIN FETCH i.producto p
           LEFT JOIN FETCH p.categoria
           LEFT JOIN FETCH p.region
           LEFT JOIN FETCH i.turnos
           WHERE m.sede.id = :sedeId
             AND m.fecha = :fecha
             AND m.estado = :estado
           """)
    Optional<MenuDia> findPublicadoWithDetails(@Param("sedeId") Integer sedeId,
                                               @Param("fecha") LocalDate fecha,
                                               @Param("estado") String estado);

    @Query("""
           SELECT DISTINCT m FROM MenuDia m
           JOIN FETCH m.sede
           LEFT JOIN FETCH m.items i
           LEFT JOIN FETCH i.producto
           LEFT JOIN FETCH i.turnos
           WHERE m.id = :id
           """)
    Optional<MenuDia> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
           SELECT m FROM MenuDia m
           JOIN FETCH m.sede
           WHERE m.sede.id = :sedeId
           ORDER BY m.fecha DESC
           """)
    List<MenuDia> findBySedeIdOrderByFechaDesc(@Param("sedeId") Integer sedeId);

    Optional<MenuDia> findBySedeIdAndFecha(Integer sedeId, LocalDate fecha);
}
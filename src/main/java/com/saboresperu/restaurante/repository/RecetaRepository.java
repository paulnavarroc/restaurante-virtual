package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecetaRepository extends JpaRepository<Receta, Integer> {

    @Query("""
           SELECT r FROM Receta r
           JOIN FETCH r.detalles d
           JOIN FETCH d.insumo
           WHERE r.producto.id = :productoId AND r.activa = true
           """)
    Optional<Receta> findByProductoIdAndActivaTrue(@Param("productoId") Integer productoId);

    Optional<Receta> findByProductoId(Integer productoId);

    @Query("""
           SELECT r FROM Receta r
           JOIN FETCH r.producto
           LEFT JOIN FETCH r.detalles d
           LEFT JOIN FETCH d.insumo
           WHERE r.producto.id = :productoId
           """)
    Optional<Receta> findByProductoIdWithDetalles(@Param("productoId") Integer productoId);
}
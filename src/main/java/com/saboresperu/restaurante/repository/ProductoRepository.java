package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    @Query("""
           SELECT p FROM Producto p
           JOIN FETCH p.categoria
           LEFT JOIN FETCH p.region
           WHERE p.activo = true
           """)
    List<Producto> findByActivoTrue();

    @Query("""
           SELECT p FROM Producto p
           JOIN FETCH p.categoria
           LEFT JOIN FETCH p.region
           WHERE p.id = :id
           """)
    Optional<Producto> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
           SELECT p FROM Producto p
           JOIN FETCH p.categoria
           LEFT JOIN FETCH p.region
           ORDER BY p.nombre ASC
           """)
    List<Producto> findAllWithDetails();

    List<Producto> findByActivoTrueOrderByNombreAsc();

    List<Producto> findAllByOrderByNombreAsc();

}
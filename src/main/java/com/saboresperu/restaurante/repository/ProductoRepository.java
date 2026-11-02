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

    @Query("""
           SELECT DISTINCT p FROM Producto p
           JOIN FETCH p.categoria c
           LEFT JOIN FETCH p.region r
           WHERE p.activo = true
             AND (:regionId IS NULL OR r.id = :regionId)
             AND (:categoriaId IS NULL OR c.id = :categoriaId)
             AND (
                  :busqueda IS NULL
                  OR :busqueda = ''
                  OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  OR LOWER(COALESCE(p.descripcion, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
             )
           ORDER BY p.nombre ASC
           """)
    List<Producto> findDisponibles(@Param("regionId") Integer regionId,
                                   @Param("categoriaId") Integer categoriaId,
                                   @Param("busqueda") String busqueda);
}
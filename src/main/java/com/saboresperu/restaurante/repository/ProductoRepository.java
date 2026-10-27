package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByActivoTrueAndDisponibleTrue();

    List<Producto> findByRegionIdAndActivoTrueAndDisponibleTrue(Integer regionId);

    List<Producto> findByCategoriaIdAndActivoTrueAndDisponibleTrue(Integer categoriaId);
    @Query("""
           SELECT DISTINCT p FROM Producto p
           JOIN FETCH p.region
           JOIN FETCH p.categoria
           WHERE p.activo = true AND p.disponible = true
             AND (:regionId IS NULL OR p.region.id = :regionId)
             AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
             AND (
                  :busqueda IS NULL
                  OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:busqueda AS string), '%'))
                  OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', CAST(:busqueda AS string), '%'))
             )
           """)
    List<Producto> buscarProductos(@Param("regionId") Integer regionId,
                                   @Param("categoriaId") Integer categoriaId,
                                   @Param("busqueda") String busqueda);

    @Query("""
           SELECT p FROM Producto p
           JOIN FETCH p.region
           JOIN FETCH p.categoria
           WHERE p.id = :id
           """)
    Optional<Producto> findByIdWithDetails(@Param("id") Integer id);

    

}
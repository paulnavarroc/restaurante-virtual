package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("""
           SELECT p FROM Product p
           JOIN FETCH p.category
           LEFT JOIN FETCH p.region
           WHERE p.isActive = true
             AND (:categoryId IS NULL OR p.category.id = :categoryId)
             AND (:regionId IS NULL OR p.region.id = :regionId)
             AND (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')))
           ORDER BY p.name
           """)
    List<Product> findDisponibles(@Param("regionId") Integer regionId, @Param("categoryId") Integer categoryId, @Param("q") String q);

    @Query("""
           SELECT p FROM Product p
           JOIN FETCH p.category
           LEFT JOIN FETCH p.region
           WHERE p.id = :id
           """)
    Optional<Product> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
       SELECT p FROM Product p
       JOIN FETCH p.category
       LEFT JOIN FETCH p.region
       ORDER BY p.name
       """)
    List<Product> findAllWithDetails();

    @Query("""
       SELECT p FROM Product p
       JOIN FETCH p.category
       LEFT JOIN FETCH p.region
       WHERE p.isActive = true
       ORDER BY p.name
       """)
    List<Product> findAllActiveWithDetails();
}

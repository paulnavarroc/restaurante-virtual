package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Cupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CuponRepository extends JpaRepository<Cupon, Integer> {
    @Query("""
           SELECT c FROM Cupon c
           JOIN FETCH c.promocion p
           WHERE UPPER(c.codigo) = UPPER(:codigo)
             AND c.activo = true
             AND p.activa = true
           """)
    Optional<Cupon> findActivoByCodigo(@Param("codigo") String codigo);

    Optional<Cupon> findByCodigoIgnoreCaseAndActivoTrue(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    @Query("SELECT c FROM Cupon c JOIN FETCH c.promocion ORDER BY c.codigo")
    List<Cupon> findAllWithPromocion();
}
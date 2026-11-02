package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.UsuarioAcceso;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UsuarioAccesoRepository extends JpaRepository<UsuarioAcceso, Long> {

    List<UsuarioAcceso> findByUsuarioIdOrderByFechaDesc(Integer usuarioId, Pageable pageable);

    @Query("""
           SELECT a FROM UsuarioAcceso a
           LEFT JOIN FETCH a.usuario
           ORDER BY a.fecha DESC
           """)
    List<UsuarioAcceso> findRecientes(Pageable pageable);
}
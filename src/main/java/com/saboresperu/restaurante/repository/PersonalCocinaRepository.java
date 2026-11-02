package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.PersonalCocina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PersonalCocinaRepository extends JpaRepository<PersonalCocina, Integer> {

    Optional<PersonalCocina> findByUsuarioId(Integer usuarioId);

    @Query("""
           SELECT p FROM PersonalCocina p
           JOIN FETCH p.sede
           JOIN FETCH p.usuario
           WHERE p.usuario.id = :usuarioId AND p.activo = true
           """)
    Optional<PersonalCocina> findActivoByUsuarioId(@Param("usuarioId") Integer usuarioId);
}
package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);

    @Query("""
           SELECT DISTINCT u FROM Usuario u
           LEFT JOIN FETCH u.roles
           ORDER BY u.fechaRegistro DESC
           """)
    List<Usuario> findAllWithRoles();

    @Query("""
           SELECT DISTINCT u FROM Usuario u
           JOIN u.roles r
           LEFT JOIN FETCH u.roles
           WHERE r.nombre = :rol
           ORDER BY u.fechaRegistro DESC
           """)
    List<Usuario> findByRolNombre(@Param("rol") String rol);
}
package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Cliente;
import com.saboresperu.restaurante.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByUsuario(Usuario usuario);
    Optional<Cliente> findByUsuarioId(Integer usuarioId);

    @Query("""
    SELECT c FROM Cliente c
    JOIN FETCH c.usuario u
    ORDER BY c.fechaRegistro DESC
    """)
    List<Cliente> findAllWithUsuario();
}
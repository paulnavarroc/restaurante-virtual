package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.DireccionCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DireccionRepository extends JpaRepository<DireccionCliente, Integer> {
    @Query("""
       SELECT d FROM DireccionCliente d
       JOIN FETCH d.distrito
       WHERE d.cliente.id = :clienteId AND d.activa = true
       ORDER BY d.esPrincipal DESC
       """)
    List<DireccionCliente> findByClienteIdAndActivaTrueOrderByEsPrincipalDesc(@Param("clienteId") Integer clienteId);



}
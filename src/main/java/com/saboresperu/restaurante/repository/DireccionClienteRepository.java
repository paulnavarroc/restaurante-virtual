package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.DireccionCliente;
import com.saboresperu.restaurante.entity.UbigeoDistrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DireccionClienteRepository extends JpaRepository<DireccionCliente, Integer> {
    @Query("""
       SELECT d FROM DireccionCliente d
       JOIN FETCH d.distrito
       WHERE d.cliente.id = :clienteId AND d.activa = true
       ORDER BY d.esPrincipal DESC
       """)
    List<DireccionCliente> findByClienteIdAndActivaTrueOrderByEsPrincipalDesc(@Param("clienteId") Integer clienteId);

    @Query("""
       SELECT d FROM UbigeoDistrito d
       JOIN FETCH d.provincia p
       JOIN FETCH p.departamento
       WHERE d.id = :id
       """)
    Optional<UbigeoDistrito> findByIdWithJerarquia(@Param("id") String id);


    Optional<DireccionCliente> findByIdAndClienteIdAndActivaTrue(Integer id, Integer clienteId);
}
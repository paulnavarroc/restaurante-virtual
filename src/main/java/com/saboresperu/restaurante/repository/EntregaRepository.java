package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EntregaRepository extends JpaRepository<Entrega, Integer> {

    Optional<Entrega> findByPedidoId(Integer pedidoId);

    @Query("""
           SELECT e FROM Entrega e
           JOIN FETCH e.pedido p
           JOIN FETCH p.estadoPedido
           JOIN FETCH p.direccion
           JOIN FETCH p.cliente c
           JOIN FETCH c.usuario
           LEFT JOIN FETCH p.detalles d
           LEFT JOIN FETCH d.producto
           LEFT JOIN FETCH e.repartidor r
           LEFT JOIN FETCH r.usuario
           WHERE e.repartidor.id = :repartidorId
             AND e.estado IN :estados
           ORDER BY e.fechaAsignacion ASC NULLS LAST
           """)
    List<Entrega> findByRepartidorAndEstados(@Param("repartidorId") Integer repartidorId,
                                             @Param("estados") List<String> estados);

    @Query("""
           SELECT e FROM Entrega e
           JOIN FETCH e.pedido p
           JOIN FETCH p.estadoPedido
           JOIN FETCH p.direccion
           JOIN FETCH p.cliente c
           JOIN FETCH c.usuario
           LEFT JOIN FETCH e.repartidor
           WHERE e.estado = :estado
             AND p.sede.id = :sedeId
           """)
    List<Entrega> findPendientesBySede(@Param("sedeId") Integer sedeId,
                                       @Param("estado") String estado);

    List<Entrega> findByRepartidorIdAndEstadoIn(Integer repartidorId, List<String> estados);
    List<Entrega> findByEstado(String estado);
}
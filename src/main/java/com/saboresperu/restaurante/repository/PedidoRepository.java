package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Pedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query("""
           SELECT DISTINCT p FROM Pedido p
           JOIN FETCH p.estadoPedido
           JOIN FETCH p.sede
           JOIN FETCH p.direccion d
           LEFT JOIN FETCH d.distrito
           LEFT JOIN FETCH p.detalles det
           LEFT JOIN FETCH det.producto
           WHERE p.cliente.id = :clienteId
           ORDER BY p.fechaPedido DESC
           """)
    List<Pedido> findByClienteIdWithDetails(@Param("clienteId") Integer clienteId);

    @Query("""
           SELECT DISTINCT p FROM Pedido p
           JOIN FETCH p.estadoPedido
           JOIN FETCH p.sede
           JOIN FETCH p.direccion
           LEFT JOIN FETCH p.detalles det
           LEFT JOIN FETCH det.producto
           WHERE p.id = :id AND p.cliente.id = :clienteId
           """)
    Optional<Pedido> findByIdAndClienteIdWithDetails(@Param("id") Integer id,
                                                     @Param("clienteId") Integer clienteId);

    @Query("""
           SELECT DISTINCT p FROM Pedido p
           JOIN FETCH p.estadoPedido
           JOIN FETCH p.sede
           JOIN FETCH p.direccion
           LEFT JOIN FETCH p.detalles det
           LEFT JOIN FETCH det.producto
           WHERE p.sede.id = :sedeId
             AND p.estadoPedido.nombre IN :estados
           ORDER BY p.fechaPedido ASC
           """)
    List<Pedido> findBySedeIdAndEstadosWithDetails(@Param("sedeId") Integer sedeId,
                                                   @Param("estados") List<String> estados);

    @Query("""
           SELECT DISTINCT p FROM Pedido p
           JOIN FETCH p.estadoPedido
           JOIN FETCH p.sede
           JOIN FETCH p.cliente
           LEFT JOIN FETCH p.detalles det
           LEFT JOIN FETCH det.producto
           WHERE p.id = :id
           """)
    Optional<Pedido> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
       SELECT COUNT(p) FROM Pedido p
       WHERE p.fechaPedido >= :desde AND p.fechaPedido < :hasta
         AND (:sedeId IS NULL OR p.sede.id = :sedeId)
         AND p.estadoPedido.nombre <> 'CANCELADO'
       """)
    long countPedidosEntre(@Param("desde") LocalDateTime desde,
                           @Param("hasta") LocalDateTime hasta,
                           @Param("sedeId") Integer sedeId);

    @Query("""
       SELECT COALESCE(SUM(p.total), 0) FROM Pedido p
       WHERE p.fechaPedido >= :desde AND p.fechaPedido < :hasta
         AND (:sedeId IS NULL OR p.sede.id = :sedeId)
         AND p.estadoPedido.nombre <> 'CANCELADO'
       """)
    BigDecimal sumVentasEntre(@Param("desde") LocalDateTime desde,
                              @Param("hasta") LocalDateTime hasta,
                              @Param("sedeId") Integer sedeId);

    @Query("""
       SELECT COUNT(p) FROM Pedido p
       WHERE (:sedeId IS NULL OR p.sede.id = :sedeId)
         AND p.estadoPedido.nombre IN :estados
       """)
    long countByEstados(@Param("sedeId") Integer sedeId,
                        @Param("estados") List<String> estados);

    @Query("""
       SELECT p.estadoPedido.nombre, COUNT(p) FROM Pedido p
       WHERE p.fechaPedido >= :desde AND p.fechaPedido < :hasta
         AND (:sedeId IS NULL OR p.sede.id = :sedeId)
       GROUP BY p.estadoPedido.nombre
       """)
    List<Object[]> countAgrupadoPorEstadoHoy(@Param("desde") LocalDateTime desde,
                                             @Param("hasta") LocalDateTime hasta,
                                             @Param("sedeId") Integer sedeId);

    @Query("""
       SELECT DISTINCT p FROM Pedido p
       JOIN FETCH p.estadoPedido
       JOIN FETCH p.sede
       JOIN FETCH p.cliente c
       JOIN FETCH c.usuario
       WHERE p.fechaPedido >= :desde
         AND (:sedeId IS NULL OR p.sede.id = :sedeId)
       ORDER BY p.fechaPedido DESC
       """)
    List<Pedido> findRecientes(@Param("desde") LocalDateTime desde,
                               @Param("sedeId") Integer sedeId,
                               Pageable pageable);
}
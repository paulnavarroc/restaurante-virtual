package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByClienteIdOrderByFechaPedidoDesc(Integer clienteId);

    List<Pedido> findByEstadoPedidoNombre(String nombreEstado);

    @Query("""
       SELECT DISTINCT p FROM Pedido p
       JOIN FETCH p.estadoPedido
       JOIN FETCH p.direccion
       LEFT JOIN FETCH p.detalles d
       LEFT JOIN FETCH d.producto
       WHERE p.estadoPedido.nombre IN :estados
       ORDER BY p.fechaPedido DESC
       """)
    List<Pedido> findByEstadosWithDetails(@Param("estados") List<String> estados);

    @Query("SELECT p FROM Pedido p WHERE p.estadoPedido.nombre IN :estados ORDER BY p.fechaPedido ASC")
    List<Pedido> findByEstados(@Param("estados") List<String> estados);

    Optional<Pedido> findByIdAndClienteId(Integer id, Integer clienteId);

    @Query("""
       SELECT DISTINCT p FROM Pedido p
       JOIN FETCH p.estadoPedido
       JOIN FETCH p.direccion
       LEFT JOIN FETCH p.detalles d
       LEFT JOIN FETCH d.producto
       WHERE p.cliente.id = :clienteId
       ORDER BY p.fechaPedido DESC
       """)
    List<Pedido> findByClienteIdWithDetails(@Param("clienteId") Integer clienteId);

    @Query("""
       SELECT DISTINCT p FROM Pedido p
       JOIN FETCH p.estadoPedido
       JOIN FETCH p.direccion
       LEFT JOIN FETCH p.detalles d
       LEFT JOIN FETCH d.producto
       WHERE p.id = :id AND p.cliente.id = :clienteId
       """)
    Optional<Pedido> findByIdAndClienteIdWithDetails(@Param("id") Integer id,
                                                     @Param("clienteId") Integer clienteId);
}
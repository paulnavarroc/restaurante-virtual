package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {

    Optional<Delivery> findByOrderId(Integer orderId);

    @Query("""
           SELECT d FROM Delivery d
           JOIN FETCH d.order o
           JOIN FETCH o.orderStatus
           JOIN FETCH o.address a
           LEFT JOIN FETCH a.district
           JOIN FETCH o.branch
           WHERE o.branch.id = :branchId
             AND d.status = 'PENDING'
           ORDER BY o.orderedAt ASC
           """)
    List<Delivery> findPendingByBranchId(@Param("branchId") Integer branchId);

    @Query("""
           SELECT d FROM Delivery d
           JOIN FETCH d.order o
           JOIN FETCH o.orderStatus
           JOIN FETCH o.address a
           LEFT JOIN FETCH a.district
           WHERE d.courier.id = :courierId
             AND d.status IN ('ASSIGNED', 'ON_ROUTE')
           ORDER BY d.assignedAt ASC
           """)
    List<Delivery> findActiveByCourierId(@Param("courierId") Integer courierId);

    @Query("""
           SELECT d FROM Delivery d
           JOIN FETCH d.order o
           JOIN FETCH o.orderStatus
           JOIN FETCH o.address
           JOIN FETCH o.branch
           LEFT JOIN FETCH d.courier c
           LEFT JOIN FETCH c.user
           WHERE d.id = :id
           """)
    Optional<Delivery> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
           SELECT COUNT(d) FROM Delivery d
           WHERE d.courier.id = :courierId
             AND d.status IN ('ASSIGNED', 'ON_ROUTE')
           """)
    long countActiveByCourierId(@Param("courierId") Integer courierId);

    @Query("""
       SELECT COUNT(d) FROM Delivery d
       WHERE d.status IN ('PENDING', 'ASSIGNED', 'ON_ROUTE')
       """)
    long countOpenDeliveries();
}
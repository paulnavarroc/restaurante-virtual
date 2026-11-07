package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("""
           SELECT o FROM Order o
           JOIN FETCH o.orderStatus
           JOIN FETCH o.branch
           WHERE o.customer.id = :customerId
           ORDER BY o.orderedAt DESC
           """)
    List<Order> findByCustomerIdWithStatus(@Param("customerId") Integer customerId);

    @Query("""
           SELECT DISTINCT o FROM Order o
           JOIN FETCH o.orderStatus
           JOIN FETCH o.customer c
           JOIN FETCH c.user
           LEFT JOIN FETCH o.items i
           LEFT JOIN FETCH i.product
           WHERE o.id = :id AND o.customer.id = :customerId
           """)
    Optional<Order> findByIdAndCustomerIdWithDetails(@Param("id") Integer id, @Param("customerId") Integer customerId);

    @Query("""
           SELECT DISTINCT o FROM Order o
           JOIN FETCH o.orderStatus
           WHERE o.branch.id = :branchId
           ORDER BY o.orderedAt DESC
           """)
    List<Order> findByBranchIdWithStatus(@Param("branchId") Integer branchId);

    @Query("""
       SELECT COUNT(o) FROM Order o
       WHERE o.orderedAt >= :start AND o.orderedAt < :end
       """)
    long countBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
       SELECT COALESCE(SUM(o.total), 0) FROM Order o
       WHERE o.orderedAt >= :start AND o.orderedAt < :end
         AND o.orderStatus.code <> 'CANCELLED'
       """)
    BigDecimal sumSalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
       SELECT COUNT(o) FROM Order o
       WHERE o.orderStatus.code IN :codes
       """)
    long countByStatusCodes(@Param("codes") Collection<String> codes);

    @Query("""
       SELECT o FROM Order o
       JOIN FETCH o.orderStatus
       JOIN FETCH o.customer c
       JOIN FETCH c.user
       ORDER BY o.orderedAt DESC
       """)
    List<Order> findRecent(Pageable pageable);

    @Query("""
       SELECT DISTINCT o FROM Order o
       JOIN FETCH o.orderStatus
       JOIN FETCH o.branch
       JOIN FETCH o.customer c
       JOIN FETCH c.user
       WHERE (:statusCode IS NULL OR o.orderStatus.code = :statusCode)
         AND (:branchId IS NULL OR o.branch.id = :branchId)
       ORDER BY o.orderedAt DESC
       """)
    List<Order> searchAdmin(@Param("statusCode") String statusCode, @Param("branchId") Integer branchId, Pageable pageable);

    @Query("""
       SELECT DISTINCT o FROM Order o
       JOIN FETCH o.orderStatus
       JOIN FETCH o.branch
       JOIN FETCH o.customer c
       JOIN FETCH c.user
       JOIN FETCH o.address a
       LEFT JOIN FETCH a.district
       LEFT JOIN FETCH o.items
       WHERE o.id = :id
       """)
    Optional<Order> findByIdForAdmin(@Param("id") Integer id);

    long countByCustomerId(Integer customerId);

    @Query("""
       SELECT o FROM Order o
       JOIN FETCH o.orderStatus
       JOIN FETCH o.branch
       WHERE o.customer.id = :customerId
       ORDER BY o.orderedAt DESC
       """)
    List<Order> findRecentByCustomerId(@Param("customerId") Integer customerId, Pageable pageable);

    long countByOrderedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("""
       SELECT COALESCE(SUM(o.total), 0) FROM Order o
       WHERE o.orderedAt >= :from AND o.orderedAt <= :to
         AND o.orderStatus.code <> 'CANCELLED'
       """)
    BigDecimal sumTotalByOrderedAtBetween(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus.code = :code")
    long countByStatusCode(@Param("code") String code);

    @Query("""
       SELECT o FROM Order o
       JOIN FETCH o.orderStatus
       JOIN FETCH o.customer c
       JOIN FETCH c.user
       ORDER BY o.orderedAt DESC
       """)
    List<Order> findRecentWithStatus(Pageable pageable);
}
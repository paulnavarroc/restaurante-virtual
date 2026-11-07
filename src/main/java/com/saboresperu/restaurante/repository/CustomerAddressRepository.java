package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Integer> {

    List<CustomerAddress> findByCustomerIdAndIsActiveTrueOrderByIsPrimaryDesc(Integer customerId);

    @Query("""
           SELECT a FROM CustomerAddress a
           JOIN FETCH a.district d
           JOIN FETCH d.province p
           JOIN FETCH p.department
           WHERE a.id = :id AND a.customer.id = :customerId
           """)
    Optional<CustomerAddress> findByIdAndCustomerIdWithDistrict(@Param("id") Integer id, @Param("customerId") Integer customerId);

    @Query("""
       SELECT a FROM CustomerAddress a
       LEFT JOIN FETCH a.district
       WHERE a.customer.id = :customerId AND a.isActive = true
       ORDER BY a.isPrimary DESC
       """)
    List<CustomerAddress> findActiveWithDistrict(@Param("customerId") Integer customerId);
}
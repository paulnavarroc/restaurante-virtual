package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByUserId(Integer userId);

    @Query("""
           SELECT c FROM Customer c
           JOIN FETCH c.user
           ORDER BY c.createdAt DESC
           """)
    List<Customer> findAllWithUser();

    @Query("""
       SELECT c FROM Customer c
       JOIN FETCH c.user u
       WHERE c.id = :id
       """)
    Optional<Customer> findByIdWithUser(@Param("id") Integer id);

    @Query("""
       SELECT c FROM Customer c
       JOIN FETCH c.user u
       WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :q, '%'))
          OR c.documentNumber LIKE CONCAT('%', :q, '%')
       ORDER BY c.createdAt DESC
       """)
    List<Customer> search(@Param("q") String q);
}
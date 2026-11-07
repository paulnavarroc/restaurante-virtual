package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
           SELECT u FROM User u
           LEFT JOIN FETCH u.roles
           WHERE u.email = :email
           """)
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query("""
           SELECT u FROM User u
           LEFT JOIN FETCH u.roles
           WHERE u.id = :id
           """)
    Optional<User> findByIdWithRoles(@Param("id") Integer id);

    @Query("""
           SELECT DISTINCT u FROM User u
           LEFT JOIN FETCH u.roles
           ORDER BY u.createdAt DESC
           """)
    List<User> findAllWithRoles();

    @Query("""
       SELECT DISTINCT u FROM User u
       LEFT JOIN FETCH u.roles
       WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :q, '%'))
       ORDER BY u.createdAt DESC
       """)
    List<User> searchWithRoles(@Param("q") String q);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countByRoleId(@Param("roleId") Integer roleId);
}
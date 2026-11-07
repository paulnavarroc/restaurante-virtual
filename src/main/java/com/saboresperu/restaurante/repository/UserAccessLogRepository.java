package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.UserAccessLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserAccessLogRepository extends JpaRepository<UserAccessLog, Long> {

    List<UserAccessLog> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    @Query("""
           SELECT a FROM UserAccessLog a
           LEFT JOIN FETCH a.user
           ORDER BY a.createdAt DESC
           """)
    List<UserAccessLog> findRecientes(Pageable pageable);
}
package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.SecurityConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface SecurityConfigRepository extends JpaRepository<SecurityConfig, Integer> {

    /** Fila única del sistema. */
    default Optional<SecurityConfig> findSingleton() {
        return findById(1);
    }

    /**
     * Lectura con bloqueo al hacer bump (evita dos bumps concurrentes
     * que lean la misma versión).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM SecurityConfig c WHERE c.id = :id")
    Optional<SecurityConfig> findByIdForUpdate(@Param("id") Integer id);
}
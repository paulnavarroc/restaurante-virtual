package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SedeRepository extends JpaRepository<Sede, Integer> {
    List<Sede> findByActivaTrue();
}
package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.SedeTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SedeTurnoRepository extends JpaRepository<SedeTurno, Integer> {
    List<SedeTurno> findBySedeIdAndActivoTrue(Integer sedeId);
}
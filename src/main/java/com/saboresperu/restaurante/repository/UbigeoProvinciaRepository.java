package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.UbigeoProvincia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UbigeoProvinciaRepository extends JpaRepository<UbigeoProvincia, String> {
    List<UbigeoProvincia> findByDepartamentoIdOrderByNombreAsc(String idDepartamento);
}
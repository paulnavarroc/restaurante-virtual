package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.UbigeoDistrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UbigeoDistritoRepository extends JpaRepository<UbigeoDistrito, String> {
    List<UbigeoDistrito> findByProvinciaIdOrderByNombreAsc(String idProvincia);
}
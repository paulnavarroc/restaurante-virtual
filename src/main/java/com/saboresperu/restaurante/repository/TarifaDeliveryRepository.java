package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.TarifaDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TarifaDeliveryRepository extends JpaRepository<TarifaDelivery, Integer> {
    List<TarifaDelivery> findBySedeIdOrderByKmDesdeAsc(Integer sedeId);
}
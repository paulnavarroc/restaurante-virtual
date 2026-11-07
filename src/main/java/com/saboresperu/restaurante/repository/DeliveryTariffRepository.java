package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.DeliveryTariff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryTariffRepository extends JpaRepository<DeliveryTariff, Integer> {
    List<DeliveryTariff> findByBranchIdAndIsActiveTrueOrderBySortOrderAsc(Integer branchId);
}
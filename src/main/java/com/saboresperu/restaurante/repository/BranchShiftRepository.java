package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.BranchShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BranchShiftRepository extends JpaRepository<BranchShift, Integer> {
    List<BranchShift> findByBranchIdAndIsActiveTrue(Integer branchId);
}
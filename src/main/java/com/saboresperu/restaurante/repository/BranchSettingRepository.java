package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.BranchSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchSettingRepository extends JpaRepository<BranchSetting, Integer> {
    List<BranchSetting> findByBranchId(Integer branchId);
    Optional<BranchSetting> findByBranchIdAndSettingKey(Integer branchId, String settingKey);
}

package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.GeoProvince;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeoProvinceRepository extends JpaRepository<GeoProvince, String> {
    List<GeoProvince> findByDepartmentId(String departmentId);
    List<GeoProvince> findByDepartmentIdOrderByNameAsc(String departmentId);
}

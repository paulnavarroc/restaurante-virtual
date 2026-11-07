package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.GeoDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GeoDistrictRepository extends JpaRepository<GeoDistrict, String> {
    List<GeoDistrict> findByProvinceId(String provinceId);

    @Query("""
           SELECT d FROM GeoDistrict d
           JOIN FETCH d.province p
           JOIN FETCH p.department
           WHERE d.id = :id
           """)
    Optional<GeoDistrict> findByIdWithParents(@Param("id") String id);
}

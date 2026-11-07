package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.GeoDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeoDepartmentRepository extends JpaRepository<GeoDepartment, String> {}
package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByIsActiveTrueOrderByNameAsc();
    Optional<Category> findByName(String name);
}
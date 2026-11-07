package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
    List<MenuItem> findByIsActiveTrueAndContextOrderBySortOrderAsc(String context);
    List<MenuItem> findByIsActiveTrueOrderByContextAscSortOrderAsc();
}
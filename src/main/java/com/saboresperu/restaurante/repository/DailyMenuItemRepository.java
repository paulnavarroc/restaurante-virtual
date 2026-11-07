package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.DailyMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DailyMenuItemRepository extends JpaRepository<DailyMenuItem, Integer> {
    List<DailyMenuItem> findByDailyMenuId(Integer dailyMenuId);

    @Query("""
       SELECT i FROM DailyMenuItem i
       LEFT JOIN FETCH i.shifts
       JOIN FETCH i.product
       WHERE i.id = :id
       """)
    Optional<DailyMenuItem> findByIdWithShifts(@Param("id") Integer id);
}
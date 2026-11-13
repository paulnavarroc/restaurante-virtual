package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.DailyMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyMenuRepository extends JpaRepository<DailyMenu, Integer> {

    @Query("""
           SELECT m FROM DailyMenu m
           JOIN FETCH m.branch
           LEFT JOIN FETCH m.items i
           LEFT JOIN FETCH i.product
           WHERE m.id = :id
           """)
    Optional<DailyMenu> findByIdWithItems(@Param("id") Integer id);

    //@Query("SELECT m FROM DailyMenu m JOIN FETCH m.branch LEFT JOIN FETCH m.items")
    @Query("""
       SELECT m FROM DailyMenu m
       JOIN FETCH m.branch
       ORDER BY m.menuDate DESC, m.branch.name
       """)
    List<DailyMenu> findAllWithBranch();

    @Query("""
           SELECT m FROM DailyMenu m
           JOIN FETCH m.branch
           LEFT JOIN FETCH m.items i
           LEFT JOIN FETCH i.product
           WHERE m.branch.id = :branchId
             AND m.menuDate = :menuDate
             AND m.status = :status
           """)
    Optional<DailyMenu> findPublishedWithItems(@Param("branchId") Integer branchId, @Param("menuDate") LocalDate menuDate, @Param("status") String status);

    @Query("""
       SELECT DISTINCT m FROM DailyMenu m
       JOIN FETCH m.branch
       LEFT JOIN FETCH m.items i
       LEFT JOIN FETCH i.product
       WHERE m.id = :id
       """)
    Optional<DailyMenu> findByIdWithItemsAndProduct(@Param("id") Integer id);

    Optional<DailyMenu> findByBranchIdAndMenuDate(Integer branchId, LocalDate menuDate);

    @Query("""
       SELECT DISTINCT m FROM DailyMenu m
       JOIN FETCH m.branch
       JOIN FETCH m.items i
       JOIN FETCH i.product p
       JOIN FETCH p.category
       LEFT JOIN FETCH p.region
       LEFT JOIN FETCH i.shifts
       WHERE m.branch.id = :branchId
         AND m.menuDate = :menuDate
         AND m.status = 'PUBLISHED'
       """)
    Optional<DailyMenu> findPublishedWithProducts(@Param("branchId") Integer branchId, @Param("menuDate") LocalDate menuDate);

    @Query("""
       SELECT DISTINCT m FROM DailyMenu m
       JOIN FETCH m.branch
       LEFT JOIN FETCH m.items i
       LEFT JOIN FETCH i.product
       LEFT JOIN FETCH i.shifts
       WHERE m.id = :id
       """)
    Optional<DailyMenu> findByIdWithItemsProductAndShifts(@Param("id") Integer id);
}
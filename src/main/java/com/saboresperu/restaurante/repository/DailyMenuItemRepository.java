package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.DailyMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying(clearAutomatically = true)
    @Query("""
       UPDATE DailyMenuItem i
          SET i.soldQuantity = COALESCE(i.soldQuantity, 0) + :qty,
              i.isAvailable = CASE
                  WHEN i.maxQuota IS NOT NULL
                       AND COALESCE(i.soldQuantity, 0) + :qty >= i.maxQuota
                  THEN false
                  ELSE i.isAvailable
              END
        WHERE i.id = :id
          AND i.isAvailable = true
          AND (i.maxQuota IS NULL
               OR COALESCE(i.soldQuantity, 0) + :qty <= i.maxQuota)
       """)
    int incrementSoldIfAvailable(@Param("id") Integer id, @Param("qty") int qty);
}
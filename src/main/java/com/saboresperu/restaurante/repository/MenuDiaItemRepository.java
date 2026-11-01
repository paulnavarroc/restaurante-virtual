package com.saboresperu.restaurante.repository;

import com.saboresperu.restaurante.entity.MenuDiaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MenuDiaItemRepository extends JpaRepository<MenuDiaItem, Integer> {
    @Query("""
       SELECT i FROM MenuDiaItem i
       JOIN FETCH i.producto
       JOIN FETCH i.menuDia m
       JOIN FETCH m.sede
       WHERE i.id = :id
       """)
    Optional<MenuDiaItem> findByIdWithProducto(@Param("id") Integer id);
}

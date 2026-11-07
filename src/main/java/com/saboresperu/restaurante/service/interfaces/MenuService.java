package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.ProductCardDto;

import java.util.List;

public interface MenuService {
    /**
     * @param branchId sede; null = primera activa
     * @param regionId filtro opcional
     * @param categoryId filtro opcional
     * @param q búsqueda por nombre
     */
    List<ProductCardDto> listTodayMenu(Integer branchId, Integer regionId, Integer categoryId, String q);

    boolean hasPublishedMenuToday(Integer branchId);
}
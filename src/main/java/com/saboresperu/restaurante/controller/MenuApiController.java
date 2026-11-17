package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.ProductCardDto;
import com.saboresperu.restaurante.service.interfaces.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuApiController {

    private final MenuService menuService;

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) Integer regionId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String q) {

        boolean hasMenu = menuService.hasPublishedMenuToday(branchId);
        List<ProductCardDto> products = hasMenu
                ? menuService.listTodayMenu(branchId, regionId, categoryId, q)
                : List.of();

        Map<String, Object> body = new HashMap<>();
        body.put("hasMenu", hasMenu);
        body.put("products", products);
        return body;
    }
}
package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.entity.Branch;
import com.saboresperu.restaurante.repository.BranchRepository;
import com.saboresperu.restaurante.repository.CategoryRepository;
import com.saboresperu.restaurante.repository.RegionRepository;
import com.saboresperu.restaurante.service.impl.BranchShiftResolver;
import com.saboresperu.restaurante.service.interfaces.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalTime;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MenuService menuService;
    private final CategoryRepository categoryRepository;
    private final RegionRepository regionRepository;
    private final BranchRepository branchRepository;
    private final BranchShiftResolver branchShiftResolver;

    @GetMapping("/")
    public String home(@RequestParam(required = false) Integer branchId, Model model) {
        model.addAttribute("titulo", "Inicio");
        model.addAttribute("hasMenu", menuService.hasPublishedMenuToday(branchId));
        model.addAttribute("products", menuService.listTodayMenu(branchId, null, null, null));
        model.addAttribute("branches", branchRepository.findByIsActiveTrue());
        model.addAttribute("branchId", branchId);
        return "home";
    }

    @GetMapping("/menu")
    public String menu(@RequestParam(required = false) Integer branchId,
                       @RequestParam(required = false) Integer regionId,
                       @RequestParam(required = false) Integer categoryId,
                       @RequestParam(required = false) String q,
                       Model model) {
        Integer bid = branchId;
        if (bid == null) {
            bid = branchRepository.findByIsActiveTrue().stream()
                    .findFirst().map(Branch::getId).orElse(null);
        }
        String shiftLabel = null;
        if (bid != null) {

            shiftLabel = branchShiftResolver.currentShift(bid, LocalTime.now())
                    .map(s -> switch (s.getShift()) {
                        case "BREAKFAST" -> "Desayuno";
                        case "LUNCH" -> "Almuerzo";
                        case "DINNER" -> "Cena";
                        default -> s.getShift();
                    })
                    .orElse("Fuera de turno configurado");
        }
        model.addAttribute("currentShiftLabel", shiftLabel);
        model.addAttribute("titulo", "Menú del día");
        model.addAttribute("hasMenu", menuService.hasPublishedMenuToday(branchId));
        model.addAttribute("products", menuService.listTodayMenu(branchId, regionId, categoryId, q));
        model.addAttribute("categories", categoryRepository.findByIsActiveTrueOrderByNameAsc());
        model.addAttribute("regions", regionRepository.findByIsActiveTrueOrderByNameAsc());
        model.addAttribute("branches", branchRepository.findByIsActiveTrue());
        model.addAttribute("branchId", branchId);
        model.addAttribute("regionId", regionId);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("q", q);
        return "menu/index";
    }
}
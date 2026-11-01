package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.service.interfaces.DashboardService;
import com.saboresperu.restaurante.service.interfaces.SedeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final SedeService sedeService;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(@RequestParam(required = false) Integer sedeId, Model model) {
        model.addAttribute("sedes", sedeService.listarActivas());
        model.addAttribute("dashboard", dashboardService.obtener(sedeId));
        model.addAttribute("sedeId", sedeId);
        return "admin/dashboard";
    }
}
package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping({ "", "/", "/dashboard" })
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_VIEW')")
    public String dashboard(Model model) {
        model.addAttribute("titulo", "Panel de control");
        model.addAttribute("kpi", dashboardService.getTodayKpis());
        return "admin/dashboard";
    }
}
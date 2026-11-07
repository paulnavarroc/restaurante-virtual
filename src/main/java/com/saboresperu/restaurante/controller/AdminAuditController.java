package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.service.interfaces.AccessAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AccessAuditService accessAuditService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERM_ACCESS_LOGS_VIEW','PERM_SECURITY_AUDIT_VIEW')")
    public String index(@RequestParam(defaultValue = "access") String tab, Model model) {
        model.addAttribute("titulo", "Auditoría");
        model.addAttribute("tab", tab);

        if ("security".equals(tab)) {
            model.addAttribute("securityLogs", accessAuditService.listSecurityAudits(100));
        } else {
            model.addAttribute("accessLogs", accessAuditService.listAccessLogs(100));
        }
        return "admin/audit/index";
    }
}
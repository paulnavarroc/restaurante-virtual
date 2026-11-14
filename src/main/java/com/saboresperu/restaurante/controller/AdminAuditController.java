package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.AccessLogRowDto;
import com.saboresperu.restaurante.dto.SecurityAuditRowDto;
import com.saboresperu.restaurante.service.interfaces.AccessAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    /*
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

     */

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERM_ACCESS_LOGS_VIEW','PERM_SECURITY_AUDIT_VIEW')")
    public String index(@RequestParam(defaultValue = "access") String tab,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        Model model) {

        if (page < 0) {
            page = 0;
        }
        if (size < 1 || size > 100) {
            size = 20;
        }

        model.addAttribute("titulo", "Auditoría");
        model.addAttribute("tab", tab);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        Pageable pageable = PageRequest.of(page, size);

        if ("security".equals(tab)) {
            Page<SecurityAuditRowDto> result = accessAuditService.pageSecurityAudits(pageable);
            model.addAttribute("securityLogs", result.getContent());
            model.addAttribute("pageData", result);
        } else {
            Page<AccessLogRowDto> result = accessAuditService.pageAccessLogs(pageable);
            model.addAttribute("accessLogs", result.getContent());
            model.addAttribute("pageData", result);
        }

        return "admin/audit/index";
    }
}
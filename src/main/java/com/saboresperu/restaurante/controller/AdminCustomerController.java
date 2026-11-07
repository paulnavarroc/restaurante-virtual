package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.service.interfaces.AdminCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_CUSTOMERS_VIEW')")
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("titulo", "Clientes");
        model.addAttribute("customers", adminCustomerService.search(q));
        model.addAttribute("q", q);
        return "admin/customers/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_CUSTOMERS_VIEW')")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("titulo", "Cliente");
        model.addAttribute("customer", adminCustomerService.getDetail(id));
        return "admin/customers/detail";
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PERM_CUSTOMERS_EDIT')")
    public String deactivate(@PathVariable Integer id, RedirectAttributes ra) {
        adminCustomerService.setUserActive(id, false);
        ra.addFlashAttribute("mensaje", "Cliente desactivado (no podrá iniciar sesión)");
        return "redirect:/admin/customers/" + id;
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('PERM_CUSTOMERS_EDIT')")
    public String activate(@PathVariable Integer id, RedirectAttributes ra) {
        adminCustomerService.setUserActive(id, true);
        ra.addFlashAttribute("mensaje", "Cliente activado");
        return "redirect:/admin/customers/" + id;
    }
}
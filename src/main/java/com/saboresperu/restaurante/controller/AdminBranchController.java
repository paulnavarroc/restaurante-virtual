package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.BranchFormDto;
import com.saboresperu.restaurante.service.interfaces.AdminBranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/branches")
@RequiredArgsConstructor
public class AdminBranchController {

    private final AdminBranchService adminBranchService;

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_BRANCHES_VIEW')")
    public String list(Model model) {
        model.addAttribute("branches", adminBranchService.listAll());
        return "admin/branches/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('PERM_BRANCHES_MANAGE')")
    public String createForm(Model model) {
        model.addAttribute("form", new BranchFormDto(
                null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("5.00"),
                null, null, true));
        return "admin/branches/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_BRANCHES_MANAGE')")
    public String create(@Valid @ModelAttribute("form") BranchFormDto form,
                         BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            return "admin/branches/form";
        }
        try {
            adminBranchService.create(form);
            ra.addFlashAttribute("mensaje", "Sede creada");
            return "redirect:/admin/branches";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "admin/branches/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('PERM_BRANCHES_MANAGE')")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("form", adminBranchService.getForm(id));
        return "admin/branches/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_BRANCHES_MANAGE')")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") BranchFormDto form,
                         BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            return "admin/branches/form";
        }
        try {
            adminBranchService.update(id, form);
            ra.addFlashAttribute("mensaje", "Sede actualizada");
            return "redirect:/admin/branches";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "admin/branches/form";
        }
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('PERM_BRANCHES_MANAGE')")
    public String activate(@PathVariable Integer id, RedirectAttributes ra) {
        adminBranchService.setActive(id, true);
        ra.addFlashAttribute("mensaje", "Sede activada");
        return "redirect:/admin/branches";
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PERM_BRANCHES_MANAGE')")
    public String deactivate(@PathVariable Integer id, RedirectAttributes ra) {
        adminBranchService.setActive(id, false);
        ra.addFlashAttribute("mensaje", "Sede desactivada");
        return "redirect:/admin/branches";
    }
}
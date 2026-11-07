package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.domain.PromoTypes;
import com.saboresperu.restaurante.dto.CouponFormDto;
import com.saboresperu.restaurante.service.interfaces.AdminCouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final AdminCouponService adminCouponService;

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_COUPONS_VIEW')")
    public String list(Model model) {
        model.addAttribute("titulo", "Cupones");
        model.addAttribute("coupons", adminCouponService.listAll());
        return "admin/coupons/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('PERM_COUPONS_MANAGE')")
    public String createForm(Model model) {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        model.addAttribute("titulo", "Nuevo cupón");
        model.addAttribute("form", new CouponFormDto(
                null, null, null, PromoTypes.PERCENT, new BigDecimal("10"),
                null, 100, now, now.plusDays(30), true));
        model.addAttribute("discountTypes", List.of(PromoTypes.PERCENT, PromoTypes.AMOUNT));
        return "admin/coupons/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_COUPONS_MANAGE')")
    public String create(@Valid @ModelAttribute("form") CouponFormDto form,
                         BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("titulo", "Nuevo cupón");
            model.addAttribute("discountTypes", List.of(PromoTypes.PERCENT, PromoTypes.AMOUNT));
            return "admin/coupons/form";
        }
        try {
            adminCouponService.create(form);
            ra.addFlashAttribute("mensaje", "Cupón creado");
            return "redirect:/admin/coupons";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Nuevo cupón");
            model.addAttribute("discountTypes", List.of(PromoTypes.PERCENT, PromoTypes.AMOUNT));
            return "admin/coupons/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('PERM_COUPONS_MANAGE')")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("titulo", "Editar cupón");
        model.addAttribute("form", adminCouponService.getForm(id));
        model.addAttribute("discountTypes", List.of(PromoTypes.PERCENT, PromoTypes.AMOUNT));
        return "admin/coupons/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_COUPONS_MANAGE')")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") CouponFormDto form,
                         BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("titulo", "Editar cupón");
            model.addAttribute("discountTypes", List.of(PromoTypes.PERCENT, PromoTypes.AMOUNT));
            return "admin/coupons/form";
        }
        try {
            adminCouponService.update(id, form);
            ra.addFlashAttribute("mensaje", "Cupón actualizado");
            return "redirect:/admin/coupons";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Editar cupón");
            model.addAttribute("discountTypes", List.of(PromoTypes.PERCENT, PromoTypes.AMOUNT));
            return "admin/coupons/form";
        }
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PERM_COUPONS_MANAGE')")
    public String deactivate(@PathVariable Integer id, RedirectAttributes ra) {
        adminCouponService.setActive(id, false);
        ra.addFlashAttribute("mensaje", "Cupón desactivado");
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('PERM_COUPONS_MANAGE')")
    public String activate(@PathVariable Integer id, RedirectAttributes ra) {
        adminCouponService.setActive(id, true);
        ra.addFlashAttribute("mensaje", "Cupón activado");
        return "redirect:/admin/coupons";
    }
}
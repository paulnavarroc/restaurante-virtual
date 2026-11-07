package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.domain.PromoScopes;
import com.saboresperu.restaurante.domain.PromoTypes;
import com.saboresperu.restaurante.dto.PromotionFormDto;
import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.AdminPromotionService;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionController {

    private final AdminPromotionService adminPromotionService;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    private Integer currentUserId() {
        return userRepository.findByEmail(securityUtils.currentEmail())
                .map(User::getId).orElse(null);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_PROMOTIONS_VIEW')")
    public String list(Model model) {
        model.addAttribute("titulo", "Promociones");
        model.addAttribute("promotions", adminPromotionService.listAll());
        return "admin/promotions/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('PERM_PROMOTIONS_MANAGE')")
    public String createForm(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("titulo", "Nueva promoción");
        model.addAttribute("form", new PromotionFormDto(
                null, null, null, null,
                PromoTypes.PERCENT, new java.math.BigDecimal("10"), null,
                PromoScopes.ORDER, now, now.plusDays(7), 100,
                false, true, null, null, null, true,
                List.of(), List.of()));
        addCatalogs(model);
        return "admin/promotions/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_PROMOTIONS_MANAGE')")
    public String create(@Valid @ModelAttribute("form") PromotionFormDto form,
                         BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("titulo", "Nueva promoción");
            addCatalogs(model);
            return "admin/promotions/form";
        }
        try {
            adminPromotionService.create(form, currentUserId());
            ra.addFlashAttribute("mensaje", "Promoción creada");
            return "redirect:/admin/promotions";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Nueva promoción");
            addCatalogs(model);
            return "admin/promotions/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('PERM_PROMOTIONS_MANAGE')")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("titulo", "Editar promoción");
        model.addAttribute("form", adminPromotionService.getForm(id));
        addCatalogs(model);
        return "admin/promotions/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_PROMOTIONS_MANAGE')")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") PromotionFormDto form,
                         BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("titulo", "Editar promoción");
            addCatalogs(model);
            return "admin/promotions/form";
        }
        try {
            adminPromotionService.update(id, form, currentUserId());
            ra.addFlashAttribute("mensaje", "Promoción actualizada");
            return "redirect:/admin/promotions";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Editar promoción");
            addCatalogs(model);
            return "admin/promotions/form";
        }
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PERM_PROMOTIONS_MANAGE')")
    public String deactivate(@PathVariable Integer id, RedirectAttributes ra) {
        adminPromotionService.setActive(id, false, currentUserId());
        ra.addFlashAttribute("mensaje", "Promoción desactivada");
        return "redirect:/admin/promotions";
    }

    private void addCatalogs(Model model) {
        model.addAttribute("branches", branchRepository.findByIsActiveTrue());
        model.addAttribute("products", productRepository.findAllActiveWithDetails());
        model.addAttribute("categories", categoryRepository.findByIsActiveTrueOrderByNameAsc());
        model.addAttribute("promoTypes", List.of(PromoTypes.PERCENT, PromoTypes.AMOUNT));
        model.addAttribute("scopes", List.of(
                PromoScopes.PRODUCT, PromoScopes.CATEGORY,
                PromoScopes.ORDER, PromoScopes.DELIVERY));
    }
}
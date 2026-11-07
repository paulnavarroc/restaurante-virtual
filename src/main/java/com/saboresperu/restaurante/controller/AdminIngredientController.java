package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.IngredientFormDto;
import com.saboresperu.restaurante.service.interfaces.AdminIngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/ingredients")
@RequiredArgsConstructor
public class AdminIngredientController {

    private final AdminIngredientService adminIngredientService;

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_INGREDIENTS_VIEW')")
    public String list(Model model) {
        model.addAttribute("titulo", "Insumos");
        model.addAttribute("ingredients", adminIngredientService.listAll());
        return "admin/ingredients/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('PERM_INGREDIENTS_MANAGE')")
    public String createForm(Model model) {
        model.addAttribute("titulo", "Nuevo insumo");
        model.addAttribute("form", new IngredientFormDto(null, null, "kg", false, null, true));
        return "admin/ingredients/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_INGREDIENTS_MANAGE')")
    public String create(@Valid @ModelAttribute("form") IngredientFormDto form,
                         BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("titulo", "Nuevo insumo");
            return "admin/ingredients/form";
        }
        try {
            adminIngredientService.create(form);
            ra.addFlashAttribute("mensaje", "Insumo creado");
            return "redirect:/admin/ingredients";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Nuevo insumo");
            return "admin/ingredients/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('PERM_INGREDIENTS_MANAGE')")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("titulo", "Editar insumo");
        model.addAttribute("form", adminIngredientService.getForm(id));
        return "admin/ingredients/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_INGREDIENTS_MANAGE')")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") IngredientFormDto form,
                         BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("titulo", "Editar insumo");
            return "admin/ingredients/form";
        }
        try {
            adminIngredientService.update(id, form);
            ra.addFlashAttribute("mensaje", "Insumo actualizado");
            return "redirect:/admin/ingredients";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Editar insumo");
            return "admin/ingredients/form";
        }
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PERM_INGREDIENTS_MANAGE')")
    public String deactivate(@PathVariable Integer id, RedirectAttributes ra) {
        adminIngredientService.setActive(id, false);
        ra.addFlashAttribute("mensaje", "Insumo desactivado");
        return "redirect:/admin/ingredients";
    }
}
package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.ProductFormDto;
import com.saboresperu.restaurante.repository.CategoryRepository;
import com.saboresperu.restaurante.repository.RegionRepository;
import com.saboresperu.restaurante.service.interfaces.AdminProductService;
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
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;
    private final CategoryRepository categoryRepository;
    private final RegionRepository regionRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_PRODUCTS_VIEW')")
    public String list(Model model) {
        model.addAttribute("titulo", "Productos");
        model.addAttribute("products", adminProductService.listAll(null));
        return "admin/products/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('PERM_PRODUCTS_CREATE')")
    public String createForm(Model model) {
        model.addAttribute("titulo", "Nuevo producto");
        model.addAttribute("form", new ProductFormDto(
                null, null, null, null, null, BigDecimal.ZERO, null, null, true, true));
        addCatalogs(model);
        return "admin/products/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_PRODUCTS_CREATE')")
    public String create(@Valid @ModelAttribute("form") ProductFormDto form,
                         BindingResult bindingResult,
                         RedirectAttributes ra,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Nuevo producto");
            addCatalogs(model);
            return "admin/products/form";
        }
        try {
            Integer id = adminProductService.create(form);
            ra.addFlashAttribute("mensaje", "Producto creado");
            return "redirect:/admin/products";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Nuevo producto");
            addCatalogs(model);
            return "admin/products/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('PERM_PRODUCTS_EDIT')")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("titulo", "Editar producto");
        model.addAttribute("form", adminProductService.getForm(id));
        addCatalogs(model);
        return "admin/products/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_PRODUCTS_EDIT')")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") ProductFormDto form,
                         BindingResult bindingResult,
                         RedirectAttributes ra,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Editar producto");
            addCatalogs(model);
            return "admin/products/form";
        }
        try {
            adminProductService.update(id, form);
            ra.addFlashAttribute("mensaje", "Producto actualizado");
            return "redirect:/admin/products";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Editar producto");
            addCatalogs(model);
            return "admin/products/form";
        }
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PERM_PRODUCTS_DELETE')")
    public String deactivate(@PathVariable Integer id, RedirectAttributes ra) {
        adminProductService.setActive(id, false);
        ra.addFlashAttribute("mensaje", "Producto desactivado");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('PERM_PRODUCTS_EDIT')")
    public String activate(@PathVariable Integer id, RedirectAttributes ra) {
        adminProductService.setActive(id, true);
        ra.addFlashAttribute("mensaje", "Producto activado");
        return "redirect:/admin/products";
    }

    private void addCatalogs(Model model) {
        model.addAttribute("categories", categoryRepository.findByIsActiveTrueOrderByNameAsc());
        model.addAttribute("regions", regionRepository.findByIsActiveTrueOrderByNameAsc());
    }
}
package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.InsumoFormDTO;
import com.saboresperu.restaurante.service.interfaces.InsumoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/insumos")
@RequiredArgsConstructor
public class AdminInsumoController {

    private final InsumoService insumoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("insumos", insumoService.listarTodos());
        return "admin/insumos";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("insumo", InsumoFormDTO.builder()
                .activo(true).stockIlimitado(false).unidad("kg").build());
        return "admin/form-insumo";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("insumo", insumoService.obtenerForm(id));
        return "admin/form-insumo";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("insumo") InsumoFormDTO form,
                        BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) return "admin/form-insumo";
        try {
            insumoService.crear(form);
            ra.addFlashAttribute("mensaje", "Insumo creado");
            return "redirect:/admin/insumos";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/form-insumo";
        }
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id,
                             @Valid @ModelAttribute("insumo") InsumoFormDTO form,
                             BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) return "admin/form-insumo";
        try {
            insumoService.actualizar(id, form);
            ra.addFlashAttribute("mensaje", "Insumo actualizado");
            return "redirect:/admin/insumos";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/form-insumo";
        }
    }
}

package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.CuponFormDTO;
import com.saboresperu.restaurante.service.interfaces.PromocionAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/cupones")
@RequiredArgsConstructor
public class AdminCuponController {

    private final PromocionAdminService promocionAdminService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("cupones", promocionAdminService.listarCupones());
        model.addAttribute("promociones", promocionAdminService.listar()); // filtrar tipo CUPON en vista
        model.addAttribute("cupon", new CuponFormDTO());
        return "admin/cupones";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("cupon") CuponFormDTO form,
                        BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("cupones", promocionAdminService.listarCupones());
            model.addAttribute("promociones", promocionAdminService.listar());
            return "admin/cupones";
        }
        try {
            promocionAdminService.crearCupon(form);
            ra.addFlashAttribute("mensaje", "Cupón creado");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/cupones";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Integer id, @RequestParam boolean activo, RedirectAttributes ra) {
        promocionAdminService.activarCupon(id, activo);
        return "redirect:/admin/cupones";
    }
}
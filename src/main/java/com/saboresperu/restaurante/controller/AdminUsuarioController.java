package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.UsuarioAdminFormDTO;
import com.saboresperu.restaurante.service.interfaces.AdminUsuarioService;
import com.saboresperu.restaurante.service.interfaces.SedeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;
    private final SedeService sedeService;

    @GetMapping
    public String listar(@RequestParam(required = false) String rol, Model model) {
        model.addAttribute("usuarios", adminUsuarioService.listar(rol));
        model.addAttribute("rolFiltro", rol);
        return "admin/usuarios";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", UsuarioAdminFormDTO.builder().activo(true).rol("CHEF").build());
        model.addAttribute("sedes", sedeService.listarActivas());
        return "admin/form-usuario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") UsuarioAdminFormDTO form,
                        BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("sedes", sedeService.listarActivas());
            return "admin/form-usuario";
        }
        try {
            adminUsuarioService.crear(form);
            ra.addFlashAttribute("mensaje", "Usuario creado");
            return "redirect:/admin/usuarios";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("sedes", sedeService.listarActivas());
            return "admin/form-usuario";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("form", adminUsuarioService.obtenerForm(id));
        model.addAttribute("sedes", sedeService.listarActivas());
        return "admin/form-usuario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id,
                             @Valid @ModelAttribute("form") UsuarioAdminFormDTO form,
                             BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("sedes", sedeService.listarActivas());
            return "admin/form-usuario";
        }
        try {
            adminUsuarioService.actualizar(id, form);
            ra.addFlashAttribute("mensaje", "Usuario actualizado");
            return "redirect:/admin/usuarios";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("sedes", sedeService.listarActivas());
            return "admin/form-usuario";
        }
    }

    @PostMapping("/{id}/activo")
    public String activo(@PathVariable Integer id, @RequestParam boolean activo, RedirectAttributes ra) {
        adminUsuarioService.setActivo(id, activo);
        ra.addFlashAttribute("mensaje", activo ? "Usuario activado" : "Usuario desactivado");
        return "redirect:/admin/usuarios";
    }
}
package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.service.interfaces.AuditoriaAccesoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/auditoria")
@RequiredArgsConstructor
public class AdminAuditoriaController {

    private final AuditoriaAccesoService auditoriaAccesoService;

    @GetMapping
    public String recientes(Model model) {
        model.addAttribute("accesos", auditoriaAccesoService.listarRecientes(100));
        return "admin/auditoria";
    }

    @GetMapping("/usuario/{id}")
    public String porUsuario(@PathVariable Integer id, Model model) {
        model.addAttribute("accesos", auditoriaAccesoService.listarPorUsuario(id, 50));
        model.addAttribute("usuarioId", id);
        return "admin/auditoria";
    }
}
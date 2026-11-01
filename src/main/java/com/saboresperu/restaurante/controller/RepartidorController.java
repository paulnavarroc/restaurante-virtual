package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.service.interfaces.EntregaService;
import com.saboresperu.restaurante.service.interfaces.RepartidorService;
import com.saboresperu.restaurante.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/repartidor")
@RequiredArgsConstructor
public class RepartidorController {

    private final EntregaService entregaService;
    private final RepartidorService repartidorService;
    private final SecurityUtils securityUtils;

    @GetMapping("/entregas")
    public String entregas(Model model) {
        Integer uid = securityUtils.getUsuarioIdActual();
        model.addAttribute("entregas", entregaService.listarMisEntregas(uid));
        return "repartidor/entregas";
    }

    @PostMapping("/disponible")
    public String disponible(RedirectAttributes ra) {
        try {
            repartidorService.ponerDisponible(securityUtils.getUsuarioIdActual());
            ra.addFlashAttribute("mensaje", "Ahora estás disponible");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/repartidor/entregas";
    }

    @PostMapping("/offline")
    public String offline(RedirectAttributes ra) {
        try {
            repartidorService.ponerOffline(securityUtils.getUsuarioIdActual());
            ra.addFlashAttribute("mensaje", "Modo offline");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/repartidor/entregas";
    }

    @PostMapping("/entrega/{id}/tomar")
    public String tomar(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            entregaService.tomarPendiente(id, securityUtils.getUsuarioIdActual());
            ra.addFlashAttribute("mensaje", "Entrega asignada");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/repartidor/entregas";
    }

    @PostMapping("/entrega/{id}/en-camino")
    public String enCamino(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            entregaService.marcarEnCamino(id, securityUtils.getUsuarioIdActual());
            ra.addFlashAttribute("mensaje", "En camino");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/repartidor/entregas";
    }

    @PostMapping("/entrega/{id}/entregada")
    public String entregada(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            entregaService.marcarEntregada(id, securityUtils.getUsuarioIdActual());
            ra.addFlashAttribute("mensaje", "Pedido entregado");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/repartidor/entregas";
    }
}
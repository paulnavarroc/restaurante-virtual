package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.service.interfaces.MenuDiaService;
import com.saboresperu.restaurante.service.interfaces.ProductoService;
import com.saboresperu.restaurante.service.interfaces.SedeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class MenuPublicoController {

    private final MenuDiaService menuDiaService;
    private final SedeService sedeService;
    private final ProductoService productoService;

    @GetMapping({"/", "/menu"})
    public String menu(@RequestParam(required = false) Integer sedeId,
                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                       Model model) {

        if (sedeId == null) {
            var sedes = sedeService.listarActivas();
            if (sedes.isEmpty()) {
                model.addAttribute("error", "No hay sedes activas");
                return "public/menu";
            }
            sedeId = sedes.get(0).getId();
        }
        if (fecha == null) {
            fecha = LocalDate.now();
        }

        try {
            model.addAttribute("menu", menuDiaService.obtenerPublicado(sedeId, fecha));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("sedes", sedeService.listarActivas());
        model.addAttribute("sedeId", sedeId);
        model.addAttribute("fecha", fecha);
        return "public/menu";
    }


    @GetMapping("/menu/producto/{id}")
    public String detalle(@PathVariable Integer id, Model model) {
        model.addAttribute("producto", productoService.obtenerPorId(id));
        return "public/detalle-producto";
    }
}
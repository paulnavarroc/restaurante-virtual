package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.service.interfaces.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MenuPublicoController {
/*
    private final ProductoService productoService;

    @GetMapping({"/", "/home", "/menu", "/catalogo"})
    public String menu(@RequestParam(required = false) Integer regionId,
                       @RequestParam(required = false) Integer categoriaId,
                       @RequestParam(required = false) String busqueda,
                       Model model) {
        model.addAttribute("productos", productoService.listarDisponibles(regionId, categoriaId, busqueda));
        model.addAttribute("regionId", regionId);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("titulo", "Menú – Sabores Perú");
        return "public/menu";
    }


    @GetMapping("/menu/producto/{id}")
    public String detalle(@PathVariable Integer id, Model model) {
        model.addAttribute("producto", productoService.obtenerPorId(id));
        return "public/detalle-producto";
    }

 */
}

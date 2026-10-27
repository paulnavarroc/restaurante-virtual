package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.service.interfaces.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {
/*
    @GetMapping({"/", "/home"})
    public String home(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_ADMINISTRADOR".equals(role)) {
                return "redirect:/admin/dashboard";
            }
            if ("ROLE_CHEF".equals(role)) {
                return "redirect:/cocina/pedidos";
            }
            if ("ROLE_REPARTIDOR".equals(role)) {
                return "redirect:/repartidor/entregas";
            }
            if ("ROLE_CLIENTE".equals(role)) {
                return "redirect:/cliente/catalogo";
            }
        }

        // Autenticado pero sin rol reconocido
        return "redirect:/login?error=sin_rol";
    }
    
 */

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
}
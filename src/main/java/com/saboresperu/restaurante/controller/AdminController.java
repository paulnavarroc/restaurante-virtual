package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.PedidoResponseDTO;
import com.saboresperu.restaurante.dto.ProductoDTO;
import com.saboresperu.restaurante.dto.ProductoRequestDTO;
import com.saboresperu.restaurante.service.interfaces.PedidoService;
import com.saboresperu.restaurante.service.interfaces.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductoService productoService;
    private final PedidoService pedidoService;

    // ========== DASHBOARD ==========
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        List<PedidoResponseDTO> pedidosRecientes = pedidoService.listarPorEstados(
                List.of("PENDIENTE", "CONFIRMADO", "EN_PREPARACION", "LISTO", "EN_CAMINO")
        );
        model.addAttribute("pedidosRecientes", pedidosRecientes);
        return "admin/dashboard";
    }

    // ========== PRODUCTOS ==========
    @GetMapping("/productos")
    public String listarProductos(Model model) {
        List<ProductoDTO> productos = productoService.listarDisponibles(null, null, null);
        model.addAttribute("productos", productos);
        return "admin/productos";
    }

    @GetMapping("/productos/nuevo")
    public String mostrarCrearProducto(Model model) {
        model.addAttribute("producto", new ProductoRequestDTO());
        return "admin/form-producto";
    }

    @PostMapping("/productos")
    public String crearProducto(@Valid @ModelAttribute("producto") ProductoRequestDTO dto,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/form-producto";
        }
        productoService.crear(dto);
        redirectAttributes.addFlashAttribute("mensaje", "Producto creado correctamente");
        return "redirect:/admin/productos";
    }

    @GetMapping("/productos/{id}/editar")
    public String mostrarEditarProducto(@PathVariable Integer id, Model model) {
        ProductoDTO p = productoService.obtenerPorId(id);

        ProductoRequestDTO form = new ProductoRequestDTO();
        form.setNombre(p.getNombre());
        form.setDescripcion(p.getDescripcion());
        form.setPrecio(p.getPrecio());
        form.setImagenUrl(p.getImagenUrl());
        form.setTiempoPreparacion(p.getTiempoPreparacion());
        form.setDisponible(p.getDisponible());

        // Mapeo simple por nombre (mejorable después)
        form.setRegionId(switch (p.getRegion()) {
            case "COSTA" -> 1;
            case "SIERRA" -> 2;
            case "SELVA" -> 3;
            default -> null;
        });
        form.setCategoriaId(switch (p.getCategoria()) {
            case "ENTRADAS" -> 1;
            case "PLATOS DE FONDO" -> 2;
            case "POSTRES" -> 3;
            case "BEBIDAS" -> 4;
            case "COMBOS" -> 5;
            default -> null;
        });

        model.addAttribute("producto", form);
        model.addAttribute("productoId", id);
        return "admin/form-producto";
    }

    @PostMapping("/productos/{id}")
    public String actualizarProducto(@PathVariable Integer id,
                                     @Valid @ModelAttribute("producto") ProductoRequestDTO dto,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/form-producto";
        }
        productoService.actualizar(id, dto);
        redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado");
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/{id}/disponibilidad")
    public String cambiarDisponibilidad(@PathVariable Integer id,
                                        @RequestParam boolean disponible,
                                        RedirectAttributes redirectAttributes) {
        productoService.cambiarDisponibilidad(id, disponible);
        redirectAttributes.addFlashAttribute("mensaje", "Disponibilidad actualizada");
        return "redirect:/admin/productos";
    }

    // ========== PEDIDOS ==========
    @GetMapping("/pedidos")
    public String listarPedidos(Model model) {
        List<PedidoResponseDTO> pedidos = pedidoService.listarPorEstados(
                List.of("PENDIENTE", "CONFIRMADO", "EN_PREPARACION", "LISTO", "EN_CAMINO", "ENTREGADO", "CANCELADO")
        );
        model.addAttribute("pedidos", pedidos);
        return "admin/pedidos";
    }
}
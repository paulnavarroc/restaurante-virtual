package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.AgregarMenuItemRequestDTO;
import com.saboresperu.restaurante.dto.CrearMenuDiaRequestDTO;
import com.saboresperu.restaurante.repository.ProductoRepository;
import com.saboresperu.restaurante.repository.SedeTurnoRepository;
import com.saboresperu.restaurante.service.interfaces.MenuDiaAdminService;
import com.saboresperu.restaurante.service.interfaces.SedeService;
import com.saboresperu.restaurante.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/menus")
@RequiredArgsConstructor
public class AdminMenuDiaController {

    private final MenuDiaAdminService menuDiaAdminService;
    private final SedeService sedeService;
    private final ProductoRepository productoRepository;
    private final SedeTurnoRepository sedeTurnoRepository;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String listar(@RequestParam(required = false) Integer sedeId, Model model) {
        var sedes = sedeService.listarActivas();
        if (sedeId == null && !sedes.isEmpty()) {
            sedeId = sedes.get(0).getId();
        }
        model.addAttribute("sedes", sedes);
        model.addAttribute("sedeId", sedeId);
        if (sedeId != null) {
            model.addAttribute("menus", menuDiaAdminService.listarPorSede(sedeId));
        }
        return "admin/menus-dia";
    }

    @PostMapping
    public String crear(@RequestParam Integer sedeId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                        RedirectAttributes ra) {
        try {
            var menu = menuDiaAdminService.crearBorrador(
                    CrearMenuDiaRequestDTO.builder().sedeId(sedeId).fecha(fecha).build());
            ra.addFlashAttribute("mensaje", "Menú borrador creado");
            return "redirect:/admin/menus/" + menu.getId();
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/menus?sedeId=" + sedeId;
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Integer id, Model model) {
        var menu = menuDiaAdminService.obtener(id);
        model.addAttribute("menu", menu);
        model.addAttribute("productos", productoRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("turnos", sedeTurnoRepository.findBySedeIdAndActivoTrue(menu.getSedeId()));
        return "admin/menu-dia-detalle";
    }

    @PostMapping("/{id}/items")
    public String agregarItem(@PathVariable Integer id,
                              @RequestParam Integer productoId,
                              @RequestParam(required = false) BigDecimal precioDia,
                              @RequestParam(required = false) Integer cupoMaximo,
                              @RequestParam(required = false) List<Integer> sedeTurnoIds,
                              RedirectAttributes ra) {
        try {
            menuDiaAdminService.agregarItem(id, AgregarMenuItemRequestDTO.builder()
                    .productoId(productoId)
                    .precioDia(precioDia)
                    .cupoMaximo(cupoMaximo)
                    .sedeTurnoIds(sedeTurnoIds)
                    .build());
            ra.addFlashAttribute("mensaje", "Plato agregado");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/menus/" + id;
    }

    @PostMapping("/{id}/items/{itemId}/eliminar")
    public String quitar(@PathVariable Integer id, @PathVariable Integer itemId, RedirectAttributes ra) {
        try {
            menuDiaAdminService.quitarItem(id, itemId);
            ra.addFlashAttribute("mensaje", "Plato quitado");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/menus/" + id;
    }

    @PostMapping("/items/{itemId}/disponible")
    public String disponible(@PathVariable Integer itemId,
                             @RequestParam boolean disponible,
                             @RequestParam Integer menuId,
                             RedirectAttributes ra) {
        menuDiaAdminService.marcarDisponible(itemId, disponible);
        ra.addFlashAttribute("mensaje", disponible ? "Disponible" : "Marcado agotado");
        return "redirect:/admin/menus/" + menuId;
    }

    @PostMapping("/{id}/publicar")
    public String publicar(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            menuDiaAdminService.publicar(id, securityUtils.getUsuarioIdActual());
            ra.addFlashAttribute("mensaje", "Menú publicado");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/menus/" + id;
    }

    @PostMapping("/{id}/cerrar")
    public String cerrar(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            menuDiaAdminService.cerrar(id);
            ra.addFlashAttribute("mensaje", "Menú cerrado");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/menus/" + id;
    }
}
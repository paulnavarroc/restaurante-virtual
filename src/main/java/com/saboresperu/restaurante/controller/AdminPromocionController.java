package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.PromocionFormDTO;
import com.saboresperu.restaurante.repository.ProductoRepository;
import com.saboresperu.restaurante.service.interfaces.PromocionAdminService;
import com.saboresperu.restaurante.service.interfaces.SedeService;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/promociones")
@RequiredArgsConstructor
public class AdminPromocionController {

    private final PromocionAdminService promocionAdminService;
    private final SedeService sedeService;
    private final ProductoRepository productoRepository;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("promociones", promocionAdminService.listar());
        return "admin/promociones";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("promo", PromocionFormDTO.builder()
                .activa(true)
                .combinable(true)
                .requiereCodigo(false)
                .prioridad(100)
                .fechaHoraInicio(LocalDateTime.now())
                .fechaHoraFin(LocalDateTime.now().plusDays(30))
                .build());
        cargarCatalogos(model);
        return "admin/form-promocion";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("promo", promocionAdminService.obtenerForm(id));
        cargarCatalogos(model);
        return "admin/form-promocion";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("promo") PromocionFormDTO form,
                        BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            cargarCatalogos(model);
            return "admin/form-promocion";
        }
        try {
            Integer id = promocionAdminService.crear(form, securityUtils.getUsuarioIdActual());
            ra.addFlashAttribute("mensaje", "Promoción creada");
            return "redirect:/admin/promociones/" + id + "/editar";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            cargarCatalogos(model);
            return "admin/form-promocion";
        }
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id,
                             @Valid @ModelAttribute("promo") PromocionFormDTO form,
                             BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            cargarCatalogos(model);
            return "admin/form-promocion";
        }
        try {
            promocionAdminService.actualizar(id, form, securityUtils.getUsuarioIdActual());
            ra.addFlashAttribute("mensaje", "Promoción actualizada");
            return "redirect:/admin/promociones";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            cargarCatalogos(model);
            return "admin/form-promocion";
        }
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Integer id, @RequestParam boolean activa, RedirectAttributes ra) {
        promocionAdminService.activar(id, activa, securityUtils.getUsuarioIdActual());
        ra.addFlashAttribute("mensaje", activa ? "Activada" : "Desactivada");
        return "redirect:/admin/promociones";
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("sedes", sedeService.listarActivas());
        model.addAttribute("productos", productoRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("tipos", List.of(
                "PORCENTAJE_PRODUCTO", "MONTO_PRODUCTO", "HAPPY_HOUR",
                "CUPON", "DELIVERY_GRATIS", "NX_M"
        ));
    }
}
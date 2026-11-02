package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.EntradaStockRequestDTO;
import com.saboresperu.restaurante.dto.InsumoDTO;
import com.saboresperu.restaurante.service.interfaces.InsumoService;
import com.saboresperu.restaurante.service.interfaces.SedeService;
import com.saboresperu.restaurante.service.interfaces.StockService;
import com.saboresperu.restaurante.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/stock")
@RequiredArgsConstructor
public class AdminStockController {

    private final StockService stockService;
    private final InsumoService insumoService;
    private final SedeService sedeService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String listar(@RequestParam(required = false) Integer sedeId, Model model) {
        var sedes = sedeService.listarActivas();
        model.addAttribute("sedes", sedes);

        if (sedeId == null && !sedes.isEmpty()) {
            sedeId = sedes.get(0).getId();
        }
        model.addAttribute("sedeId", sedeId);

        if (sedeId != null) {
            model.addAttribute("stocks", stockService.listarStockPorSede(sedeId));
        }
        return "admin/stock";
    }

    @GetMapping("/entrada")
    public String formEntrada(@RequestParam(required = false) Integer sedeId, Model model) {
        model.addAttribute("form", EntradaStockRequestDTO.builder().sedeId(sedeId).build());
        model.addAttribute("sedes", sedeService.listarActivas());
        model.addAttribute("insumos", insumosActivos());
        return "admin/form-entrada-stock";
    }

    @PostMapping("/entrada")
    public String registrarEntrada(@ModelAttribute("form") EntradaStockRequestDTO form,
                                   Model model,
                                   RedirectAttributes ra) {
        try {
            if (form.getSedeId() == null || form.getInsumoId() == null
                    || form.getCantidad() == null
                    || form.getCantidad().signum() <= 0) {
                throw new IllegalArgumentException("Sede, insumo y cantidad positiva son obligatorios");
            }
            stockService.registrarEntrada(form, securityUtils.getUsuarioIdActual());
            ra.addFlashAttribute("mensaje", "Entrada de stock registrada correctamente");
            return "redirect:/admin/stock?sedeId=" + form.getSedeId();
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("sedes", sedeService.listarActivas());
            model.addAttribute("insumos", insumosActivos());
            return "admin/form-entrada-stock";
        }
    }

    private List<InsumoDTO> insumosActivos() {
        return insumoService.listarTodos().stream()
                .filter(i -> Boolean.TRUE.equals(i.getActivo()))
                .toList();
    }
}
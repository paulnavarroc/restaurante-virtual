package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.RecetaDTO;
import com.saboresperu.restaurante.dto.RecetaDetalleFormDTO;
import com.saboresperu.restaurante.dto.RecetaFormDTO;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.InsumoRepository;
import com.saboresperu.restaurante.repository.ProductoRepository;
import com.saboresperu.restaurante.service.interfaces.RecetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/productos/{productoId}/receta")
@RequiredArgsConstructor
public class AdminRecetaController {

    private final RecetaService recetaService;
    private final InsumoRepository insumoRepository;
    private final ProductoRepository productoRepository;

    @GetMapping
    public String form(@PathVariable Integer productoId, Model model) {
        var producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        RecetaDTO receta = recetaService.obtenerPorProducto(productoId);

        RecetaFormDTO form = RecetaFormDTO.builder()
                .productoId(productoId)
                .productoNombre(producto.getNombre())
                .version(receta.getVersion())
                .activa(receta.getActiva() != null ? receta.getActiva() : true)
                .detalles(receta.getDetalles() == null ? new ArrayList<>()
                        : receta.getDetalles().stream()
                        .map(d -> RecetaDetalleFormDTO.builder()
                                .insumoId(d.getInsumoId())
                                .cantidad(d.getCantidad())
                                .build())
                        .collect(Collectors.toCollection(ArrayList::new)))
                .build();
        if (form.getDetalles().isEmpty()) {
            form.getDetalles().add(new RecetaDetalleFormDTO());
        }

        model.addAttribute("receta", form);
        model.addAttribute("insumos", insumoRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("stockIlimitado", Boolean.TRUE.equals(producto.getStockIlimitado()));
        return "admin/form-receta";
    }

    @PostMapping
    public String guardar(@PathVariable Integer productoId,
                          @ModelAttribute("receta") RecetaFormDTO form,
                          RedirectAttributes ra, Model model) {
        try {
            recetaService.guardar(productoId, form);
            ra.addFlashAttribute("mensaje", "Receta guardada");
            return "redirect:/admin/productos/" + productoId + "/editar";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("insumos", insumoRepository.findByActivoTrueOrderByNombreAsc());
            return "admin/form-receta";
        }
    }
}

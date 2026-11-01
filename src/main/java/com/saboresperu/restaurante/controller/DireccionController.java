package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.DireccionFormDTO;
import com.saboresperu.restaurante.entity.Cliente;
import com.saboresperu.restaurante.service.interfaces.ClienteService;
import com.saboresperu.restaurante.service.interfaces.DireccionService;
import com.saboresperu.restaurante.service.interfaces.UbigeoService;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cliente/direcciones")
@RequiredArgsConstructor
public class DireccionController {

    private final DireccionService direccionService;
    private final UbigeoService ubigeoService;
    private final ClienteService clienteService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String listar(Model model) {
        Cliente c = clienteActual();
        model.addAttribute("direcciones", direccionService.listarPorCliente(c.getId()));
        return "cliente/direcciones";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("direccion", new DireccionFormDTO());
        model.addAttribute("departamentos", ubigeoService.listarDepartamentos());
        return "cliente/form-direccion";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("direccion") DireccionFormDTO form,
                        BindingResult br,
                        Model model,
                        RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("departamentos", ubigeoService.listarDepartamentos());
            return "cliente/form-direccion";
        }
        try {
            direccionService.crear(clienteActual().getId(), form);
            ra.addFlashAttribute("mensaje", "Dirección guardada");
            return "redirect:/cliente/direcciones";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("departamentos", ubigeoService.listarDepartamentos());
            return "cliente/form-direccion";
        }
    }

    @PostMapping("/{id}/principal")
    public String principal(@PathVariable Integer id, RedirectAttributes ra) {
        direccionService.marcarPrincipal(id, clienteActual().getId());
        ra.addFlashAttribute("mensaje", "Dirección marcada como principal");
        return "redirect:/cliente/direcciones";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes ra) {
        direccionService.eliminar(id, clienteActual().getId());
        ra.addFlashAttribute("mensaje", "Dirección eliminada");
        return "redirect:/cliente/direcciones";
    }

    private Cliente clienteActual() {
        return clienteService.obtenerPorUsuarioId(securityUtils.getUsuarioIdActual());
    }
}
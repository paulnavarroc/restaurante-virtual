package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.CambiarEstadoPedidoDTO;
import com.saboresperu.restaurante.dto.PedidoResponseDTO;
import com.saboresperu.restaurante.service.interfaces.PedidoService;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cocina")
@RequiredArgsConstructor
public class CocinaController {

    private final PedidoService pedidoService;
    private final SecurityUtils securityUtils;

    @GetMapping({"", "/", "/pedidos"})
    public String pedidosPendientes(Model model) {
        // Estados que ve cocina
        List<PedidoResponseDTO> pedidos = pedidoService.listarPorEstados(
                List.of("PENDIENTE", "CONFIRMADO", "EN_PREPARACION")
        );
        model.addAttribute("pedidos", pedidos);
        return "cocina/pedidos";
    }

    @GetMapping("/pedido/{id}")
    public String detallePedido(@PathVariable Integer id, Model model) {
        // En un sistema real validarías permisos, aquí simplificado
        List<PedidoResponseDTO> todos = pedidoService.listarPorEstados(
                List.of("PENDIENTE", "CONFIRMADO", "EN_PREPARACION", "LISTO")
        );
        PedidoResponseDTO pedido = todos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        model.addAttribute("pedido", pedido);
        model.addAttribute("cambioEstado", new CambiarEstadoPedidoDTO());
        return "cocina/detalle-pedido";
    }

    @PostMapping("/pedido/{id}/estado")
    public String cambiarEstado(@PathVariable Integer id,
                                @RequestParam String nuevoEstado,
                                @RequestParam(required = false) String observacion,
                                RedirectAttributes redirectAttributes) {

        CambiarEstadoPedidoDTO dto = new CambiarEstadoPedidoDTO();
        dto.setNuevoEstado(nuevoEstado);
        dto.setObservacion(observacion);

        Integer usuarioId = securityUtils.getUsuarioIdActual();
        pedidoService.cambiarEstado(id, dto, usuarioId);

        redirectAttributes.addFlashAttribute("mensaje", "Estado actualizado a: " + nuevoEstado);
        return "redirect:/cocina/pedidos";
    }

}
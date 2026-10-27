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
@RequestMapping("/repartidor")
@RequiredArgsConstructor
public class RepartidorController {

    private final PedidoService pedidoService;
    private final SecurityUtils securityUtils;

    @GetMapping({"", "/", "/entregas"})
    public String entregas(Model model) {
        // Pedidos listos o en camino
        List<PedidoResponseDTO> pedidos = pedidoService.listarPorEstados(
                List.of("LISTO", "EN_CAMINO")
        );
        model.addAttribute("pedidos", pedidos);
        return "repartidor/entregas";
    }

    @GetMapping("/entrega/{id}")
    public String detalleEntrega(@PathVariable Integer id, Model model) {
        List<PedidoResponseDTO> pedidos = pedidoService.listarPorEstados(
                List.of("LISTO", "EN_CAMINO", "ENTREGADO")
        );
        PedidoResponseDTO pedido = pedidos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Entrega no encontrada"));

        model.addAttribute("pedido", pedido);
        model.addAttribute("cambioEstado", new CambiarEstadoPedidoDTO());
        return "repartidor/detalle-entrega";
    }

    @PostMapping("/entrega/{id}/estado")
    public String actualizarEstado(@PathVariable Integer id,
                                   @RequestParam String nuevoEstado,
                                   @RequestParam(required = false) String observacion,
                                   RedirectAttributes redirectAttributes) {

        CambiarEstadoPedidoDTO dto = new CambiarEstadoPedidoDTO();
        dto.setNuevoEstado(nuevoEstado);
        dto.setObservacion(observacion);

        Integer usuarioId = securityUtils.getUsuarioIdActual();
        pedidoService.cambiarEstado(id, dto, usuarioId);

        String mensaje = nuevoEstado.equals("ENTREGADO")
                ? "¡Entrega confirmada!"
                : "Pedido en camino";

        redirectAttributes.addFlashAttribute("mensaje", mensaje);
        return "redirect:/repartidor/entregas";
    }

}
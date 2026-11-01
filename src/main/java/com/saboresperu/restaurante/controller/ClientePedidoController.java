package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.CambiarEstadoPedidoDTO;
import com.saboresperu.restaurante.dto.CrearPedidoRequestDTO;
import com.saboresperu.restaurante.dto.PedidoResponseDTO;
import com.saboresperu.restaurante.entity.Cliente;
import com.saboresperu.restaurante.service.interfaces.ClienteService;
import com.saboresperu.restaurante.service.interfaces.PedidoService;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClientePedidoController {

    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final SecurityUtils securityUtils;

    @GetMapping("/pedidos")
    public String misPedidos(Model model) {
        Cliente c = clienteActual();
        model.addAttribute("pedidos", pedidoService.listarPorCliente(c.getId()));
        return "cliente/mis-pedidos";
    }

    @GetMapping("/pedido/{id}")
    public String detalle(@PathVariable Integer id, Model model) {
        Cliente c = clienteActual();
        model.addAttribute("pedido", pedidoService.obtenerPorId(id, c.getId()));
        return "cliente/detalle-pedido";
    }

    /**
     * Checkout directo (sin carrito de sesión).
     * El form de la vista debe enviar items[0].productoId, items[0].menuDiaItemId, items[0].cantidad, etc.
     */
    @PostMapping("/pedido")
    public String crear(@Valid @ModelAttribute("pedido") CrearPedidoRequestDTO dto,
                        BindingResult br,
                        RedirectAttributes ra,
                        Model model) {
        if (br.hasErrors()) {
            model.addAttribute("error", "Datos incompletos");
            return "cliente/checkout";
        }
        try {
            Cliente c = clienteActual();
            PedidoResponseDTO creado = pedidoService.crearPedido(c.getId(), dto);
            ra.addFlashAttribute("mensaje", "Pedido #" + creado.getId() + " creado");
            return "redirect:/cliente/pedido/" + creado.getId();
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pedido", dto);
            return "cliente/checkout";
        }
    }

    private Cliente clienteActual() {
        Integer usuarioId = securityUtils.getUsuarioIdActual();
        return clienteService.obtenerPorUsuarioId(usuarioId);
    }
}
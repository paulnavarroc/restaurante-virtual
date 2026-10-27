package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.CarritoDTO;
import com.saboresperu.restaurante.dto.CrearPedidoRequestDTO;
import com.saboresperu.restaurante.dto.DetallePedidoRequestDTO;
import com.saboresperu.restaurante.dto.PedidoResponseDTO;
import com.saboresperu.restaurante.entity.Cliente;
import com.saboresperu.restaurante.service.interfaces.CarritoService;
import com.saboresperu.restaurante.service.interfaces.ClienteService;
import com.saboresperu.restaurante.service.interfaces.DireccionService;
import com.saboresperu.restaurante.service.interfaces.PedidoService;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cliente/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;
    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final DireccionService direccionService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String verCarrito(HttpSession session, Model model) {
        model.addAttribute("carrito", carritoService.obtenerCarrito(session));
        return "cliente/carrito";
    }

    @PostMapping("/agregar")
    public String agregar(@RequestParam Integer productoId,
                          @RequestParam(defaultValue = "1") int cantidad,
                          HttpSession session,
                          RedirectAttributes ra) {
        try {
            carritoService.agregarProducto(session, productoId, cantidad);
            ra.addFlashAttribute("mensaje", "Producto agregado al carrito");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cliente/carrito";
    }

    @PostMapping("/actualizar")
    public String actualizar(@RequestParam Integer productoId,
                             @RequestParam int cantidad,
                             HttpSession session) {
        carritoService.actualizarCantidad(session, productoId, cantidad);
        return "redirect:/cliente/carrito";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer productoId, HttpSession session) {
        carritoService.eliminarProducto(session, productoId);
        return "redirect:/cliente/carrito";
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model, RedirectAttributes ra) {
        CarritoDTO carrito = carritoService.obtenerCarrito(session);
        if (carrito.isEmpty()) {
            ra.addFlashAttribute("error", "El carrito está vacío");
            return "redirect:/cliente/carrito";
        }
        Cliente cliente = obtenerClienteActual();
        model.addAttribute("carrito", carrito);
        model.addAttribute("direcciones", direccionService.listarPorCliente(cliente.getId()));
        return "cliente/checkout";
    }

    @PostMapping("/confirmar")
    public String confirmar(@RequestParam Integer direccionId,
                            @RequestParam Integer metodoPagoId,
                            @RequestParam(required = false) String observaciones,
                            HttpSession session,
                            RedirectAttributes ra) {
        CarritoDTO carrito = carritoService.obtenerCarrito(session);
        if (carrito.isEmpty()) {
            ra.addFlashAttribute("error", "El carrito está vacío");
            return "redirect:/cliente/carrito";
        }
        try {
            Cliente cliente = obtenerClienteActual();

            CrearPedidoRequestDTO dto = new CrearPedidoRequestDTO();
            dto.setDireccionId(direccionId);
            dto.setMetodoPagoId(metodoPagoId);
            dto.setObservaciones(observaciones);
            dto.setItems(carrito.getItems().stream().map(item -> {
                DetallePedidoRequestDTO d = new DetallePedidoRequestDTO();
                d.setProductoId(item.getProductoId());
                d.setCantidad(item.getCantidad());
                return d;
            }).collect(Collectors.toList()));

            PedidoResponseDTO pedido = pedidoService.crearPedido(cliente.getId(), dto);
            carritoService.limpiar(session);
            ra.addFlashAttribute("mensaje", "¡Pedido realizado!");
            return "redirect:/cliente/pedido/" + pedido.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cliente/carrito/checkout";
        }
    }

    private Cliente obtenerClienteActual() {
        Integer usuarioId = securityUtils.getUsuarioIdActual();
        if (usuarioId == null) {
            throw new RuntimeException("Debes iniciar sesión");
        }
        return clienteService.obtenerPorUsuarioId(usuarioId);
    }
}
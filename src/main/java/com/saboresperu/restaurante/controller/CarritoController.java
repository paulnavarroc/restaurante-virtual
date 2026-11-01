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
    public String ver(HttpSession session, Model model) {
        model.addAttribute("carrito", carritoService.obtener(session));
        return "cliente/carrito";
    }

    @PostMapping("/agregar")
    public String agregar(@RequestParam Integer menuDiaItemId,
                          @RequestParam(defaultValue = "1") int cantidad,
                          HttpSession session,
                          RedirectAttributes ra) {
        try {
            carritoService.agregar(session, menuDiaItemId, cantidad);
            ra.addFlashAttribute("mensaje", "Agregado al carrito");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/menu";
    }

    @PostMapping("/actualizar")
    public String actualizar(@RequestParam Integer menuDiaItemId,
                             @RequestParam int cantidad,
                             HttpSession session) {
        carritoService.actualizar(session, menuDiaItemId, cantidad);
        return "redirect:/cliente/carrito";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer menuDiaItemId, HttpSession session) {
        carritoService.eliminar(session, menuDiaItemId);
        return "redirect:/cliente/carrito";
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model, RedirectAttributes ra) {
        CarritoDTO carrito = carritoService.obtener(session);
        if (carrito.isEmpty()) {
            ra.addFlashAttribute("error", "El carrito está vacío");
            return "redirect:/cliente/carrito";
        }
        Cliente cliente = clienteActual();
        model.addAttribute("carrito", carrito);
        model.addAttribute("direcciones", direccionService.listarPorCliente(cliente.getId()));
        return "cliente/checkout";
    }

    @PostMapping("/confirmar")
    public String confirmar(@RequestParam Integer direccionId,
                            @RequestParam Integer metodoPagoId,
                            @RequestParam(required = false) String observaciones,
                            @RequestParam(required = false) String codigoCupon,
                            HttpSession session,
                            RedirectAttributes ra) {
        CarritoDTO carrito = carritoService.obtener(session);
        if (carrito.isEmpty()) {
            ra.addFlashAttribute("error", "El carrito está vacío");
            return "redirect:/cliente/carrito";
        }

        try {
            Cliente cliente = clienteActual();

            CrearPedidoRequestDTO dto = CrearPedidoRequestDTO.builder()
                    .direccionId(direccionId)
                    .metodoPagoId(metodoPagoId)
                    .observaciones(observaciones)
                    .codigoCupon(codigoCupon)
                    .items(carrito.getItems().stream().map(i ->
                            DetallePedidoRequestDTO.builder()
                                    .productoId(i.getProductoId())
                                    .menuDiaItemId(i.getMenuDiaItemId())
                                    .cantidad(i.getCantidad())
                                    .build()
                    ).collect(Collectors.toList()))
                    .build();

            PedidoResponseDTO pedido = pedidoService.crearPedido(cliente.getId(), dto);
            carritoService.limpiar(session);
            ra.addFlashAttribute("mensaje", "¡Pedido #" + pedido.getId() + " realizado!");
            return "redirect:/cliente/pedido/" + pedido.getId();
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cliente/carrito/checkout";
        }
    }

    private Cliente clienteActual() {
        return clienteService.obtenerPorUsuarioId(securityUtils.getUsuarioIdActual());
    }
}
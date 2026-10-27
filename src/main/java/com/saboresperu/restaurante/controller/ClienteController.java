package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.*;
import com.saboresperu.restaurante.entity.Cliente;
import com.saboresperu.restaurante.service.interfaces.*;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClienteController {

    private final ProductoService productoService;
    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final DireccionService direccionService;
    private final SecurityUtils securityUtils;

    // ========== CATÁLOGO ==========
    @GetMapping({"/", "/catalogo"})
    public String catalogo(@RequestParam(required = false) Integer regionId,
                           @RequestParam(required = false) Integer categoriaId,
                           @RequestParam(required = false) String busqueda,
                           Model model) {

        List<ProductoDTO> productos = productoService.listarDisponibles(regionId, categoriaId, busqueda);
        model.addAttribute("productos", productos);
        model.addAttribute("regionId", regionId);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("busqueda", busqueda);
        return "cliente/catalogo";
    }

    @GetMapping("/producto/{id}")
    public String detalleProducto(@PathVariable Integer id, Model model) {
        ProductoDTO producto = productoService.obtenerPorId(id);
        model.addAttribute("producto", producto);
        return "cliente/detalle-producto";
    }

    // ========== PEDIDOS ==========
    @GetMapping("/pedidos")
    public String misPedidos(Model model) {
        Cliente cliente = obtenerClienteActual();
        List<PedidoResponseDTO> pedidos = pedidoService.listarPorCliente(cliente.getId());
        model.addAttribute("pedidos", pedidos);
        return "cliente/mis-pedidos";
    }

    @GetMapping("/pedido/{id}")
    public String detallePedido(@PathVariable Integer id, Model model) {
        Cliente cliente = obtenerClienteActual();
        PedidoResponseDTO pedido = pedidoService.obtenerPorId(id, cliente.getId());
        model.addAttribute("pedido", pedido);
        model.addAttribute("titulo", "Pedido #" + id);
        return "cliente/detalle-pedido";
    }

    @GetMapping("/pedido/nuevo")
    public String mostrarCrearPedido(Model model) {
        Cliente cliente = obtenerClienteActual();
        model.addAttribute("pedido", new CrearPedidoRequestDTO());
        model.addAttribute("direcciones", direccionService.listarPorCliente(cliente.getId()));
        // Aquí podrías cargar métodos de pago también
        return "cliente/crear-pedido";
    }

    @PostMapping("/pedido")
    public String crearPedido(@Valid @ModelAttribute("pedido") CrearPedidoRequestDTO dto,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            Cliente cliente = obtenerClienteActual();
            model.addAttribute("direcciones", direccionService.listarPorCliente(cliente.getId()));
            return "cliente/crear-pedido";
        }

        try {
            Cliente cliente = obtenerClienteActual();
            PedidoResponseDTO pedido = pedidoService.crearPedido(cliente.getId(), dto);
            redirectAttributes.addFlashAttribute("mensaje", "Pedido creado correctamente");
            return "redirect:/cliente/pedido/" + pedido.getId();
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            Cliente cliente = obtenerClienteActual();
            model.addAttribute("direcciones", direccionService.listarPorCliente(cliente.getId()));
            return "cliente/crear-pedido";
        }
    }

    // ========== PERFIL ==========
    @GetMapping("/perfil")
    public String perfil(Model model) {
        Cliente cliente = obtenerClienteActual();
        model.addAttribute("cliente", cliente);
        model.addAttribute("direcciones", direccionService.listarPorCliente(cliente.getId()));
        return "cliente/perfil";
    }

    private Cliente obtenerClienteActual() {
        Integer usuarioId = securityUtils.getUsuarioIdActual();
        return clienteService.obtenerPorUsuarioId(usuarioId);
    }

    // ========== DIRECCIONES ==========
    @GetMapping("/direcciones")
    public String listarDirecciones(Model model) {
        Cliente cliente = obtenerClienteActual();
        model.addAttribute("direcciones", direccionService.listarPorCliente(cliente.getId()));
        return "cliente/direcciones";
    }

    @GetMapping("/direcciones/nueva")
    public String mostrarNuevaDireccion(Model model) {
        model.addAttribute("direccion", new DireccionDTO());
        return "cliente/form-direccion";
    }

    @PostMapping("/direcciones")
    public String crearDireccion(@Valid @ModelAttribute("direccion") DireccionDTO dto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) {
            return "cliente/form-direccion";
        }

        try {
            Cliente cliente = obtenerClienteActual();
            direccionService.crear(cliente.getId(), dto);
            redirectAttributes.addFlashAttribute("mensaje", "Dirección agregada correctamente");
            return "redirect:/cliente/direcciones";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "cliente/form-direccion";
        }
    }

    @PostMapping("/direcciones/{id}/principal")
    public String marcarPrincipal(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Cliente cliente = obtenerClienteActual();
        direccionService.marcarComoPrincipal(id, cliente.getId());
        redirectAttributes.addFlashAttribute("mensaje", "Dirección marcada como principal");
        return "redirect:/cliente/direcciones";
    }

    @PostMapping("/direcciones/{id}/eliminar")
    public String eliminarDireccion(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Cliente cliente = obtenerClienteActual();
        direccionService.eliminar(id, cliente.getId());
        redirectAttributes.addFlashAttribute("mensaje", "Dirección eliminada");
        return "redirect:/cliente/direcciones";
    }
}
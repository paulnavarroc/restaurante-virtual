package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.CrearPedidoRequestDTO;
import com.saboresperu.restaurante.dto.PedidoResponseDTO;
import com.saboresperu.restaurante.entity.Cliente;
import com.saboresperu.restaurante.service.interfaces.ClienteService;
import com.saboresperu.restaurante.service.interfaces.PedidoService;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dev/pedidos")
@Profile("dev")
@RequiredArgsConstructor
public class DevPedidoApiController {

    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody CrearPedidoRequestDTO dto) {
        try {
            Cliente c = clienteService.obtenerPorUsuarioId(securityUtils.getUsuarioIdActual());
            PedidoResponseDTO r = pedidoService.crearPedido(c.getId(), dto);
            return ResponseEntity.ok(r);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
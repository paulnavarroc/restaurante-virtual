package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.CambiarEstadoPedidoDTO;
import com.saboresperu.restaurante.entity.PersonalCocina;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.repository.PersonalCocinaRepository;
import com.saboresperu.restaurante.service.interfaces.PedidoService;
import com.saboresperu.restaurante.service.interfaces.SedeService;
import com.saboresperu.restaurante.util.SecurityUtils;
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
    private final SedeService sedeService;
    private final SecurityUtils securityUtils;
    private final PersonalCocinaRepository personalCocinaRepository;

    @GetMapping("/pedidos")
    public String pedidos( Model model) {

        Integer usuarioId = securityUtils.getUsuarioIdActual();
        PersonalCocina personal = personalCocinaRepository.findActivoByUsuarioId(usuarioId)
                .orElseThrow(() -> new BusinessException("No tienes sede de cocina asignada"));
        Integer sedeId = personal.getSede().getId();

        if (sedeId == null) {
            sedeId = sedeService.listarActivas().stream().findFirst().map(s -> s.getId()).orElse(null);
        }
        if (sedeId == null) {
            model.addAttribute("error", "No hay sede");
            return "cocina/pedidos";
        }
        model.addAttribute("sedeId", sedeId);
        model.addAttribute("pedidos", pedidoService.listarPorEstadosYSede(
                sedeId,
                List.of("PENDIENTE", "CONFIRMADO", "EN_PREPARACION", "LISTO")
        ));
        return "cocina/pedidos";
    }

    @PostMapping("/pedido/{id}/estado")
    public String cambiarEstado(@PathVariable Integer id,
                                @RequestParam String nuevoEstado,
                                @RequestParam(required = false) String observacion,
                                @RequestParam Integer sedeId,
                                RedirectAttributes ra) {
        try {
            CambiarEstadoPedidoDTO dto = CambiarEstadoPedidoDTO.builder()
                    .nuevoEstado(nuevoEstado)
                    .observacion(observacion)
                    .build();
            pedidoService.cambiarEstado(id, dto, securityUtils.getUsuarioIdActual());
            ra.addFlashAttribute("mensaje", "Pedido #" + id + " → " + nuevoEstado);
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cocina/pedidos?sedeId=" + sedeId;
    }
}
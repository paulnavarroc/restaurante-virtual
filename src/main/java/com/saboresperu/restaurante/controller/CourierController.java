package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.entity.Courier;
import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.CourierRepository;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.DeliveryService;
import com.saboresperu.restaurante.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/courier")
@RequiredArgsConstructor
public class CourierController {

    private final DeliveryService deliveryService;
    private final CourierRepository courierRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    private Courier requireCourier() {
        User user = userRepository.findByEmail(securityUtils.currentEmail())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        return courierRepository.findByUserId(user.getId())
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .orElseThrow(() -> new IllegalStateException("No tienes perfil de repartidor activo"));
    }

    @GetMapping("/deliveries")
    public String deliveries(Model model) {
        Courier courier = requireCourier();
        Integer branchId = courier.getBranch().getId();

        model.addAttribute("titulo", "Entregas");
        model.addAttribute("courier", courier);
        model.addAttribute("pending", deliveryService.listPendingForBranch(branchId));
        model.addAttribute("mine", deliveryService.listActiveForCourier(courier.getId()));
        return "courier/deliveries";
    }

    @PostMapping("/deliveries/{id}/claim")
    public String claim(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Courier courier = requireCourier();
            deliveryService.claim(id, courier.getId(), courier.getBranch().getId());
            ra.addFlashAttribute("mensaje", "Entrega asignada");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/courier/deliveries";
    }

    @PostMapping("/deliveries/{id}/start")
    public String start(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Courier courier = requireCourier();
            deliveryService.startRoute(id, courier.getId());
            ra.addFlashAttribute("mensaje", "En camino");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/courier/deliveries";
    }

    @PostMapping("/deliveries/{id}/delivered")
    public String delivered(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Courier courier = requireCourier();
            deliveryService.markDelivered(id, courier.getId());
            ra.addFlashAttribute("mensaje", "Pedido entregado");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/courier/deliveries";
    }

    @PostMapping("/deliveries/{id}/failed")
    public String failed(@PathVariable Integer id,
                         @RequestParam(required = false) String notes,
                         RedirectAttributes ra) {
        try {
            Courier courier = requireCourier();
            deliveryService.markFailed(id, courier.getId(), notes);
            ra.addFlashAttribute("mensaje", "Entrega marcada como fallida");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/courier/deliveries";
    }
}
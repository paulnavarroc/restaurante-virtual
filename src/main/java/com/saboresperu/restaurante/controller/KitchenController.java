package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.entity.KitchenStaff;
import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.KitchenStaffRepository;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.KitchenOrderService;
import com.saboresperu.restaurante.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/kitchen")
@RequiredArgsConstructor
public class KitchenController {

    private final KitchenOrderService kitchenOrderService;
    private final KitchenStaffRepository kitchenStaffRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    private KitchenStaff requireStaff() {
        User user = userRepository.findByEmail(securityUtils.currentEmail())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        return kitchenStaffRepository.findActiveByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("No tienes sede de cocina asignada"));
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        KitchenStaff staff = requireStaff();
        model.addAttribute("titulo", "Pedidos en cocina");
        model.addAttribute("orders",
                kitchenOrderService.listActiveForBranch(staff.getBranch().getId()));
        model.addAttribute("branch", staff.getBranch());
        return "kitchen/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateStatus(@PathVariable Integer id, @RequestParam String status, RedirectAttributes ra) {
        try {
            KitchenStaff staff = requireStaff();
            User user = staff.getUser();
            kitchenOrderService.updateStatus(id, staff.getBranch().getId(), status, user.getId());
            ra.addFlashAttribute("mensaje", "Estado actualizado");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/kitchen/orders";
    }
}
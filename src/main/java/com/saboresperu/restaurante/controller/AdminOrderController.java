package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.BranchRepository;
import com.saboresperu.restaurante.repository.OrderStatusRepository;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.AdminOrderService;
import com.saboresperu.restaurante.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private final OrderStatusRepository orderStatusRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    private Integer currentUserId() {
        return userRepository.findByEmail(securityUtils.currentEmail())
                .map(User::getId)
                .orElse(null);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_ORDERS_VIEW')")
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) Integer branchId,
                       Model model) {
        model.addAttribute("titulo", "Pedidos");
        model.addAttribute("orders", adminOrderService.search(status, branchId, 100));
        model.addAttribute("statuses", orderStatusRepository.findByIsActiveTrueOrderBySortOrderAsc());
        model.addAttribute("branches", branchRepository.findByIsActiveTrue());
        model.addAttribute("status", status);
        model.addAttribute("branchId", branchId);
        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_ORDERS_VIEW')")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("titulo", "Pedido #" + id);
        model.addAttribute("order", adminOrderService.getDetail(id));
        model.addAttribute("statuses", orderStatusRepository.findByIsActiveTrueOrderBySortOrderAsc());
        return "admin/orders/detail";
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PERM_ORDERS_STATUS')")
    public String updateStatus(@PathVariable Integer id,
                               @RequestParam String status,
                               RedirectAttributes ra) {
        try {
            adminOrderService.updateStatus(id, status, currentUserId());
            ra.addFlashAttribute("mensaje", "Estado actualizado");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}
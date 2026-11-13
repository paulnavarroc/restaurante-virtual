package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.AddToCartRequest;
import com.saboresperu.restaurante.service.interfaces.CartService;
import com.saboresperu.restaurante.service.interfaces.CustomerService;
import com.saboresperu.restaurante.util.HelperUtils;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer/cart")
@RequiredArgsConstructor
public class CustomerCartController {

    private final CartService cartService;
    private final CustomerService customerService;
    private final SecurityUtils securityUtils;
    private final HelperUtils helperUtils;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        model.addAttribute("titulo", "Carrito");

        // Cliente logueado: refresh (sesión/BD + menú de hoy)
        try {
            var customer = customerService.requireByUserEmail(securityUtils.currentEmail());
            Integer branchId = helperUtils.resolveDefaultBranchId();

            var result = cartService.refreshForCustomer(session, customer.getId(), branchId);
            model.addAttribute("cart", result.cart());
            if (result.hasChanges()) {
                model.addAttribute("cartNotice", helperUtils.buildNotice(result));
            }
        } catch (Exception ex) {
            // Invitado o sin perfil cliente
            model.addAttribute("cart", cartService.getCart(session));
        }

        Object notice = session.getAttribute("CART_NOTICE");
        if (notice != null) {
            model.addAttribute("cartNotice", notice);
            session.removeAttribute("CART_NOTICE");
        }

        return "customer/cart";
    }

    @PostMapping("/items")
    public String addItem(@Valid @ModelAttribute AddToCartRequest request,
                          HttpSession session,
                          RedirectAttributes ra,
                          @RequestHeader(value = "Referer", required = false) String referer) {
        try {
            cartService.addItem(session, request.productId(), request.dailyMenuItemId(), request.quantity());
            persistIfCustomer(session);
            ra.addFlashAttribute("mensaje", "Producto agregado al carrito");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        if (referer != null && referer.contains("/menu")) {
            return "redirect:/menu";
        }
        return "redirect:/customer/cart";
    }

    @PostMapping("/items/update")
    public String updateQuantity(@RequestParam Integer productId,
                                 @RequestParam(required = false) Integer dailyMenuItemId,
                                 @RequestParam int quantity,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        try {
            cartService.updateQuantity(session, productId, dailyMenuItemId, quantity);
            persistIfCustomer(session);
            ra.addFlashAttribute("mensaje", "Carrito actualizado");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/customer/cart";
    }

    @PostMapping("/items/remove")
    public String removeItem(@RequestParam Integer productId,
                             @RequestParam(required = false) Integer dailyMenuItemId,
                             HttpSession session,
                             RedirectAttributes ra) {
        cartService.removeItem(session, productId, dailyMenuItemId);
        persistIfCustomer(session);
        ra.addFlashAttribute("mensaje", "Producto eliminado");
        return "redirect:/customer/cart";
    }

    @PostMapping("/clear")
    public String clear(HttpSession session, RedirectAttributes ra) {
        try {
            var customer = customerService.requireByUserEmail(securityUtils.currentEmail());
            cartService.clearPersisted(customer.getId(), session);
        } catch (Exception e) {
            cartService.clear(session);
        }
        ra.addFlashAttribute("mensaje", "Carrito vacío");
        return "redirect:/customer/cart";
    }

    private void persistIfCustomer(HttpSession session) {
        try {
            var customer = customerService.requireByUserEmail(securityUtils.currentEmail());
            cartService.persistSessionCart(customer.getId(), session);
        } catch (Exception ignored) {
            // no logueado como cliente → solo sesión
        }
    }
}
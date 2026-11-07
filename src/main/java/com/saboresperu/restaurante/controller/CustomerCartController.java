package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.AddToCartRequest;
import com.saboresperu.restaurante.service.interfaces.CartService;
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

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        model.addAttribute("titulo", "Carrito");
        model.addAttribute("cart", cartService.getCart(session));
        return "customer/cart";
    }

    @PostMapping("/items")
    public String addItem(@Valid @ModelAttribute AddToCartRequest request,
                          HttpSession session,
                          RedirectAttributes ra,
                          @RequestHeader(value = "Referer", required = false) String referer) {
        try {
            cartService.addItem(session, request.productId(), request.dailyMenuItemId(), request.quantity());
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
        ra.addFlashAttribute("mensaje", "Producto eliminado");
        return "redirect:/customer/cart";
    }

    @PostMapping("/clear")
    public String clear(HttpSession session, RedirectAttributes ra) {
        cartService.clear(session);
        ra.addFlashAttribute("mensaje", "Carrito vacío");
        return "redirect:/customer/cart";
    }
}
package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.CheckoutPreviewDto;
import com.saboresperu.restaurante.dto.CheckoutRequestDto;
import com.saboresperu.restaurante.entity.Customer;
import com.saboresperu.restaurante.entity.Order;
import com.saboresperu.restaurante.service.interfaces.*;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerCheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final AddressService addressService;
    private final SecurityUtils securityUtils;
    private final PaymentService paymentService;

    @GetMapping("/checkout")
    public String checkoutForm(@RequestParam(required = false) Integer addressId,
                               @RequestParam(required = false) String couponCode,
                               HttpSession session,
                               Model model) {
        Customer customer = customerService.requireByUserEmail(securityUtils.currentEmail());
        var cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            return "redirect:/customer/cart";
        }

        CheckoutPreviewDto preview = orderService.preview(customer.getId(), addressId, couponCode, cart);

        model.addAttribute("titulo", "Confirmar pedido");
        model.addAttribute("cart", cart);
        model.addAttribute("preview", preview);
        model.addAttribute("addresses", addressService.listByCustomerId(customer.getId()));
        model.addAttribute("paymentMethods", paymentService.listActiveMethods());

        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CheckoutRequestDto(
                    addressId, null, null, couponCode));
        }
        return "customer/checkout";
    }

    @PostMapping("/checkout/preview")
    public String refreshPreview(@ModelAttribute("form") CheckoutRequestDto form) {
        String q = "";
        if (form.addressId() != null) {
            q += "addressId=" + form.addressId();
        }
        if (form.couponCode() != null && !form.couponCode().isBlank()) {
            q += (q.isEmpty() ? "" : "&") + "couponCode=" + URLEncoder.encode(form.couponCode(), UTF_8);
        }
        return "redirect:/customer/checkout" + (q.isEmpty() ? "" : "?" + q);
    }

    @PostMapping("/checkout")
    public String placeOrder(@Valid @ModelAttribute("form") CheckoutRequestDto form,
                             BindingResult bindingResult,
                             HttpSession session,
                             RedirectAttributes ra,
                             Model model) {
        Customer customer = customerService.requireByUserEmail(securityUtils.currentEmail());
        var cart = cartService.getCart(session);

        if (cart.isEmpty()) {
            return "redirect:/customer/cart";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Confirmar pedido");
            model.addAttribute("cart", cart);
            model.addAttribute("addresses", addressService.listByCustomerId(customer.getId()));
            model.addAttribute("paymentMethods", paymentService.listActiveMethods());
            return "customer/checkout";
        }

        try {
            Integer userId = customer.getUser().getId();
            Order order = orderService.placeOrder(customer.getId(), form, cart, userId);
            cartService.clear(session);
            ra.addFlashAttribute("mensaje", "Pedido #" + order.getId() + " registrado");
            return "redirect:/customer/orders/" + order.getId();
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Confirmar pedido");
            model.addAttribute("cart", cart);
            model.addAttribute("addresses", addressService.listByCustomerId(customer.getId()));
            model.addAttribute("paymentMethods", paymentService.listActiveMethods());
            return "customer/checkout";
        }
    }

}
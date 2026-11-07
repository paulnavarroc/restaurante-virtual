package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.AddressFormDto;
import com.saboresperu.restaurante.entity.Customer;
import com.saboresperu.restaurante.entity.Order;
import com.saboresperu.restaurante.repository.PaymentRepository;
import com.saboresperu.restaurante.service.interfaces.*;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final ProductService productService;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final AddressService addressService;
    private final SecurityUtils securityUtils;
    private final PaymentRepository paymentRepository;

    private Customer currentCustomer() {
        return customerService.requireByUserEmail(securityUtils.currentEmail());
    }

    @GetMapping({"/orders", "/orders/"})
    public String orders(Model model) {
        Customer customer = currentCustomer();
        model.addAttribute("titulo", "Mis pedidos");
        model.addAttribute("orders", orderService.listByCustomerId(customer.getId()));
        return "customer/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Integer id, Model model) {
        Customer customer = currentCustomer();
        model.addAttribute("titulo", "Detalle de pedido");
        model.addAttribute("order", orderService.getByIdAndCustomerId(id, customer.getId()));
        model.addAttribute("payments", paymentRepository.findByOrderIdWithMethod(order.getId()));
        return "customer/order-detail";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        model.addAttribute("titulo", "Carrito");
        return "customer/cart";
    }

    @GetMapping("/addresses")
    public String addresses(Model model) {
        Customer customer = currentCustomer();
        model.addAttribute("titulo", "Direcciones");
        model.addAttribute("addresses", addressService.listByCustomerId(customer.getId()));
        return "customer/addresses";
    }

    @GetMapping("/addresses/new")
    public String newAddress(Model model) {
        model.addAttribute("titulo", "Nueva dirección");
        model.addAttribute("form", new AddressFormDto(null, null, null, null, null, null, null, null, null));
        return "customer/address-form";
    }

    @PostMapping("/addresses")
    public String createAddress(@Valid @ModelAttribute("form") AddressFormDto form,
                                BindingResult result,
                                RedirectAttributes ra,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Nueva dirección");
            return "customer/address-form";
        }
        addressService.create(currentCustomer().getId(), form);
        ra.addFlashAttribute("mensaje", "Dirección agregada");
        return "redirect:/customer/addresses";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("titulo", "Perfil");
        model.addAttribute("customer", currentCustomer());
        return "customer/profile";
    }
}
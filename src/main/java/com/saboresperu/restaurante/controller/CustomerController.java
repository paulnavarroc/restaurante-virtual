package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.domain.SessionUserKeys;
import com.saboresperu.restaurante.dto.AddressFormDto;
import com.saboresperu.restaurante.dto.CustomerProfileFormDto;
import com.saboresperu.restaurante.entity.Customer;
import com.saboresperu.restaurante.entity.CustomerAddress;
import com.saboresperu.restaurante.entity.Order;
import com.saboresperu.restaurante.repository.GeoDepartmentRepository;
import com.saboresperu.restaurante.repository.GeoDistrictRepository;
import com.saboresperu.restaurante.repository.GeoProvinceRepository;
import com.saboresperu.restaurante.repository.PaymentRepository;
import com.saboresperu.restaurante.service.interfaces.*;
import com.saboresperu.restaurante.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
    private final GeoDepartmentRepository geoDepartmentRepository;
    private final GeoProvinceRepository geoProvinceRepository;
    private final GeoDistrictRepository geoDistrictRepository;

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
        Order order = orderService.getByIdAndCustomerId(id, customer.getId());
        model.addAttribute("titulo", "Detalle de pedido");
        model.addAttribute("order", order);
        model.addAttribute("payments", paymentRepository.findByOrderIdWithMethod(order.getId()));
        return "customer/order-detail";
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
        model.addAttribute("form", new AddressFormDto(null, null, null, null, null, null, null));
        model.addAttribute("departments", geoDepartmentRepository.findAllByOrderByNameAsc());
        return "customer/address-form";
    }

    @PostMapping("/addresses")
    public String createAddress(@Valid @ModelAttribute("form") AddressFormDto form,
                                BindingResult result,
                                RedirectAttributes ra,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Nueva dirección");
            model.addAttribute("departments", geoDepartmentRepository.findAllByOrderByNameAsc());
            return "customer/address-form";
        }
        try {
            addressService.create(currentCustomer().getId(), form);
            ra.addFlashAttribute("mensaje", "Dirección agregada");
            return "redirect:/customer/addresses";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Nueva dirección");
            model.addAttribute("departments", geoDepartmentRepository.findAllByOrderByNameAsc());
            return "customer/address-form";
        }
    }

    @GetMapping("/addresses/{id}/edit")
    public String editAddress(@PathVariable Integer id, Model model) {
        Customer customer = currentCustomer();
        CustomerAddress address = addressService.getOwned(id, customer.getId());
        if (!Boolean.TRUE.equals(address.getIsActive())) {
            throw new IllegalArgumentException("Dirección no encontrada");
        }

        AddressFormDto form = new AddressFormDto(
                address.getId(),
                address.getAlias(),
                address.getDistrict().getId(),
                address.getUrbanization(),
                address.getStreet(),
                address.getNumber(),
                address.getReference()
        );

        model.addAttribute("titulo", "Editar dirección");
        model.addAttribute("form", form);
        model.addAttribute("departments", geoDepartmentRepository.findAllByOrderByNameAsc());

        // Valores iniciales para los selects (cascada)
        String departmentId = address.getDistrict().getProvince().getDepartment().getId();
        String provinceId = address.getDistrict().getProvince().getId();
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("selectedProvinceId", provinceId);
        model.addAttribute("provinces", geoProvinceRepository.findByDepartmentIdOrderByNameAsc(departmentId));
        model.addAttribute("districts", geoDistrictRepository.findByProvinceIdOrderByNameAsc(provinceId));

        return "customer/address-form";
    }

    @PostMapping("/addresses/{id}")
    public String updateAddress(@PathVariable Integer id,
                                @Valid @ModelAttribute("form") AddressFormDto form,
                                BindingResult result,
                                RedirectAttributes ra,
                                Model model) {
        Customer customer = currentCustomer();

        if (result.hasErrors()) {
            model.addAttribute("titulo", "Editar dirección");
            model.addAttribute("departments", geoDepartmentRepository.findAllByOrderByNameAsc());
            // Si hay error, intenta reponer cascada por districtId del form
            refillGeoOnError(model, form);
            return "customer/address-form";
        }

        try {
            addressService.update(id, customer.getId(), form);
            ra.addFlashAttribute("mensaje", "Dirección actualizada");
            return "redirect:/customer/addresses";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Editar dirección");
            model.addAttribute("departments", geoDepartmentRepository.findAllByOrderByNameAsc());
            refillGeoOnError(model, form);
            return "customer/address-form";
        }
    }

    @PostMapping("/addresses/{id}/primary")
    public String markPrimary(@PathVariable Integer id, RedirectAttributes ra) {
        addressService.markAsPrimary(id, currentCustomer().getId());
        ra.addFlashAttribute("mensaje", "Dirección marcada como principal");
        return "redirect:/customer/addresses";
    }

    @PostMapping("/addresses/{id}/delete")
    public String deleteAddress(@PathVariable Integer id, RedirectAttributes ra) {
        addressService.delete(id, currentCustomer().getId());
        ra.addFlashAttribute("mensaje", "Dirección eliminada");
        return "redirect:/customer/addresses";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        Customer customer = currentCustomer();
        model.addAttribute("titulo", "Mi perfil");
        model.addAttribute("customer", customer);
        model.addAttribute("form", customerService.getProfileForm(customer.getId()));
        return "customer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("form") CustomerProfileFormDto form,
                                BindingResult result,
                                HttpServletRequest request,
                                RedirectAttributes ra,
                                Model model) {
        Customer customer = currentCustomer();

        if (result.hasErrors()) {
            model.addAttribute("titulo", "Mi perfil");
            model.addAttribute("customer", customer);
            return "customer/profile";
        }

        try {
            var update = customerService.updateProfile(customer.getId(), form);

            if (update.requiresReLogin()) {
                // Invalidar sesión y SecurityContext
                var session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                SecurityContextHolder.clearContext();

                ra.addFlashAttribute("mensaje",
                        "Perfil actualizado. Debes iniciar sesión de nuevo porque cambiaste el correo o la contraseña.");
                return "redirect:/login";
            }

            // Mismo correo y misma clave: actualizar datos visibles en sesión
            var session = request.getSession(false);
            if (session != null) {
                session.setAttribute(SessionUserKeys.DISPLAY_NAME,
                        update.firstName() + " " + update.lastName());
                session.setAttribute(SessionUserKeys.INITIALS, initials(update.firstName(), update.lastName()));
                session.setAttribute(SessionUserKeys.EMAIL, update.email());
            }

            ra.addFlashAttribute("mensaje", "Perfil actualizado correctamente");
            return "redirect:/customer/profile";

        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Mi perfil");
            model.addAttribute("customer", customer);
            return "customer/profile";
        }
    }

    private static String initials(String first, String last) {
        String a = (first != null && !first.isBlank()) ? first.substring(0, 1).toUpperCase() : "";
        String b = (last != null && !last.isBlank()) ? last.substring(0, 1).toUpperCase() : "";
        return (a + b).isEmpty() ? "?" : a + b;
    }

    /** Rehidrata provincias/distritos al fallar validación en edición/alta. */
    private void refillGeoOnError(Model model, AddressFormDto form) {
        if (form.districtId() == null || form.districtId().isBlank()) {
            model.addAttribute("provinces", List.of());
            model.addAttribute("districts", List.of());
            return;
        }
        geoDistrictRepository.findByIdWithParents(form.districtId()).ifPresentOrElse(d -> {
            String depId = d.getProvince().getDepartment().getId();
            String provId = d.getProvince().getId();
            model.addAttribute("selectedDepartmentId", depId);
            model.addAttribute("selectedProvinceId", provId);
            model.addAttribute("provinces", geoProvinceRepository.findByDepartmentIdOrderByNameAsc(depId));
            model.addAttribute("districts", geoDistrictRepository.findByProvinceIdOrderByNameAsc(provId));
        }, () -> {
            model.addAttribute("provinces", List.of());
            model.addAttribute("districts", List.of());
        });
    }
}
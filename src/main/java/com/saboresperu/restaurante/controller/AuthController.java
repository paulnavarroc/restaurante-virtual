package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.RegisterRequestDto;
import com.saboresperu.restaurante.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        @RequestParam(required = false) String securityUpdated,
                        @RequestParam(required = false) String message,
                        Authentication authentication,
                        Model model) {

        if (isAuthenticated(authentication)) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("error", (message != null && !message.isBlank()) ? message : "Correo o contraseña incorrectos");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Sesión cerrada correctamente");
        }
        if (securityUpdated != null) {
            model.addAttribute("error", "Los permisos del sistema fueron actualizados. Inicia sesión de nuevo.");
        }

        model.addAttribute("titulo", "Iniciar sesión");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Authentication authentication, Model model) {
        if (isAuthenticated(authentication)) {
            return "redirect:/";
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegisterRequestDto(null, null, null, null, null, null, null));
        }
        model.addAttribute("titulo", "Crear cuenta");
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterRequestDto form, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Crear cuenta");
            return "auth/register";
        }
        try {
            userService.registerCustomer(form);
            redirectAttributes.addFlashAttribute("mensaje", "Cuenta creada. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Crear cuenta");
            return "auth/register";
        }
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());
    }
}

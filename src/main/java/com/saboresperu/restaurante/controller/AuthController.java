package com.saboresperu.restaurante.controller;

import com.saboresperu.restaurante.dto.RegistroRequestDTO;
import com.saboresperu.restaurante.service.interfaces.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Correo o contraseña incorrectos");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Sesión cerrada");
        }
        return "auth/login";
    }

    /*

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "denied", required = false) String denied,
                        Model model) {

        if (error != null) {
            model.addAttribute("error", "Correo o contraseña incorrectos");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Sesión cerrada");
        }
        if (denied != null) {
            model.addAttribute("error", "No tienes permiso para esa sección");
        }
        return "auth/login";  // templates/auth/login.html
    }

     */


    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new RegistroRequestDTO());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("usuario") RegistroRequestDTO dto,
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            Model model) {

        if (result.hasErrors()) {
            return "auth/registro";
        }

        try {
            usuarioService.registrar(dto);
            redirectAttributes.addFlashAttribute("mensaje", "Registro exitoso");
            return "redirect:/login?registroExitoso";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/registro";
        }
    }
/*
    @GetMapping("/home")
    public String home() {
        // Redirige según el rol (se puede mejorar después)
        return "redirect:/cliente/catalogo";
    }
    
 */
}
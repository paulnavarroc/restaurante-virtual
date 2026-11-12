package com.saboresperu.restaurante.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/legal")
public class LegalController {

    @GetMapping("/terms")
    public String terms(Model model) {
        model.addAttribute("titulo", "Aviso legal");
        return "legal/terms";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute("titulo", "Política de privacidad");
        return "legal/privacy";
    }

    @GetMapping("/cookies")
    public String cookies(Model model) {
        model.addAttribute("titulo", "Política de cookies");
        return "legal/cookies";
    }
}
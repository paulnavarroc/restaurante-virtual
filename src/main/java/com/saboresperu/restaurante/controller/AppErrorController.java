package com.saboresperu.restaurante.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int code = 500;
        if (status != null) {
            try {
                code = Integer.parseInt(status.toString());
            } catch (NumberFormatException ignored) {
                code = 500;
            }
        }

        model.addAttribute("statusCode", code);
        model.addAttribute("titulo", "Error " + code);

        if (code == HttpStatus.NOT_FOUND.value()) {
            model.addAttribute("title", "Página no encontrada");
            model.addAttribute("message", "La ruta que buscas no existe o fue movida.");
            return "error/404";
        }
        if (code == HttpStatus.FORBIDDEN.value()) {
            model.addAttribute("title", "Acceso denegado");
            model.addAttribute("message", "No tienes permiso para ver este recurso.");
            return "error/403";
        }

        model.addAttribute("title", "Algo salió mal");
        model.addAttribute("message", "Ocurrió un error inesperado. Intenta de nuevo más tarde.");
        return "error/generic";
    }

    @GetMapping("/error/403")
    public String forbidden(Model model) {
        model.addAttribute("titulo", "Acceso denegado");
        model.addAttribute("title", "Acceso denegado");
        model.addAttribute("message", "No tienes permiso para ver este recurso.");
        model.addAttribute("statusCode", 403);
        return "error/403";
    }
}
package com.saboresperu.restaurante.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ========== 404 - Recurso no encontrado ==========
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model, HttpServletRequest request) {
        log.warn("Recurso no encontrado: {} - URL: {}", ex.getMessage(), request.getRequestURI());
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("status", 404);
        return "error/404";
    }

    // ========== Errores de negocio ==========
    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request) {
        log.warn("Error de negocio: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    // ========== No autorizado ==========
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUnauthorized(UnauthorizedException ex, Model model) {
        log.warn("Acceso no autorizado: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("status", 403);
        return "error/403";
    }

    // ========== Validaciones (@Valid) ==========
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public String handleValidation(Exception ex, Model model, HttpServletRequest request) {
        log.warn("Error de validación en: {}", request.getRequestURI());

        String mensaje = "Por favor corrige los errores del formulario";

        if (ex instanceof MethodArgumentNotValidException manv) {
            if (manv.getBindingResult().getFieldError() != null) {
                mensaje = manv.getBindingResult().getFieldError().getDefaultMessage();
            }
        } else if (ex instanceof BindException be) {
            if (be.getBindingResult().getFieldError() != null) {
                mensaje = be.getBindingResult().getFieldError().getDefaultMessage();
            }
        }

        model.addAttribute("error", mensaje);
        // Vuelve a la misma vista si es posible (se puede mejorar)
        return "error/validacion";
    }

    // ========== Cualquier otro error no controlado ==========
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model, HttpServletRequest request) {
        log.error("Error interno en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("error", "Ocurrió un error inesperado. Intenta nuevamente.");
        model.addAttribute("status", 500);
        return "error/500";
    }
}
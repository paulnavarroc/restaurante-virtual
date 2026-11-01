package com.saboresperu.restaurante.config;

import com.saboresperu.restaurante.dto.CarritoDTO;
import com.saboresperu.restaurante.service.interfaces.CarritoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final CarritoService carritoService;

    @ModelAttribute("carritoCount")
    public int carritoCount(HttpSession session) {
        CarritoDTO c = carritoService.obtener(session);
        return c.getTotalUnidades();
    }
}
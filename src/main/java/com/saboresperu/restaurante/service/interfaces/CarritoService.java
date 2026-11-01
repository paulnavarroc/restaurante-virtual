package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.CarritoDTO;
import jakarta.servlet.http.HttpSession;

public interface CarritoService {
    CarritoDTO obtener(HttpSession session);
    void agregar(HttpSession session, Integer menuDiaItemId, int cantidad);
    void actualizar(HttpSession session, Integer menuDiaItemId, int cantidad);
    void eliminar(HttpSession session, Integer menuDiaItemId);
    void limpiar(HttpSession session);
}
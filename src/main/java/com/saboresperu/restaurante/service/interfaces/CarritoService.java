package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.CarritoDTO;
import com.saboresperu.restaurante.dto.CarritoItemDTO;
import jakarta.servlet.http.HttpSession;

public interface CarritoService {
    CarritoDTO obtenerCarrito(HttpSession session);
    void agregarProducto(HttpSession session, Integer productoId, int cantidad);
    void actualizarCantidad(HttpSession session, Integer productoId, int cantidad);
    void eliminarProducto(HttpSession session, Integer productoId);
    void limpiar(HttpSession session);
}
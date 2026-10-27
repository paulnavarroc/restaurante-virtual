package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.CarritoDTO;
import com.saboresperu.restaurante.dto.CarritoItemDTO;
import com.saboresperu.restaurante.entity.Producto;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.ProductoRepository;
import com.saboresperu.restaurante.service.interfaces.CarritoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarritoServiceImpl implements CarritoService {

    private static final String CARRITO_SESSION_KEY = "CARRITO";

    private final ProductoRepository productoRepository;

    @Override
    public CarritoDTO obtenerCarrito(HttpSession session) {
        CarritoDTO carrito = (CarritoDTO) session.getAttribute(CARRITO_SESSION_KEY);
        if (carrito == null) {
            carrito = new CarritoDTO();
            session.setAttribute(CARRITO_SESSION_KEY, carrito);
        }
        return carrito;
    }

    @Override
    @Transactional(readOnly = true)
    public void agregarProducto(HttpSession session, Integer productoId, int cantidad) {
        if (cantidad <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a 0");
        }

        Producto producto = productoRepository.findByIdWithDetails(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (!Boolean.TRUE.equals(producto.getDisponible()) || !Boolean.TRUE.equals(producto.getActivo())) {
            throw new BusinessException("El producto no está disponible: " + producto.getNombre());
        }

        CarritoDTO carrito = obtenerCarrito(session);

        CarritoItemDTO item = CarritoItemDTO.builder()
                .productoId(producto.getId())
                .nombre(producto.getNombre())
                .imagenUrl(producto.getImagenUrl())
                .precio(producto.getPrecio())
                .cantidad(cantidad)
                .region(producto.getRegion() != null ? producto.getRegion().getNombre() : null)
                .build();

        carrito.agregarItem(item);
        session.setAttribute("CARRITO", carrito);
    }

    @Override
    public void actualizarCantidad(HttpSession session, Integer productoId, int cantidad) {
        CarritoDTO carrito = obtenerCarrito(session);
        carrito.actualizarCantidad(productoId, cantidad);
        session.setAttribute(CARRITO_SESSION_KEY, carrito);
    }

    @Override
    public void eliminarProducto(HttpSession session, Integer productoId) {
        CarritoDTO carrito = obtenerCarrito(session);
        carrito.eliminarItem(productoId);
        session.setAttribute(CARRITO_SESSION_KEY, carrito);
    }

    @Override
    public void limpiar(HttpSession session) {
        CarritoDTO carrito = obtenerCarrito(session);
        carrito.limpiar();
        session.setAttribute(CARRITO_SESSION_KEY, carrito);
    }
}
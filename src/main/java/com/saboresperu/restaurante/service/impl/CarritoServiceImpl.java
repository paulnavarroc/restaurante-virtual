package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.CarritoDTO;
import com.saboresperu.restaurante.dto.CarritoItemDTO;
import com.saboresperu.restaurante.entity.MenuDiaItem;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.MenuDiaItemRepository;
import com.saboresperu.restaurante.service.interfaces.CarritoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CarritoServiceImpl implements CarritoService {

    private final MenuDiaItemRepository menuDiaItemRepository;

    @Override
    public CarritoDTO obtener(HttpSession session) {
        CarritoDTO carrito = (CarritoDTO) session.getAttribute(CarritoDTO.SESSION_KEY);
        if (carrito == null) {
            carrito = CarritoDTO.builder().build();
            session.setAttribute(CarritoDTO.SESSION_KEY, carrito);
        }
        return carrito;
    }

    @Override
    @Transactional(readOnly = true)
    public void agregar(HttpSession session, Integer menuDiaItemId, int cantidad) {
        if (cantidad <= 0) {
            throw new BusinessException("Cantidad inválida");
        }

        MenuDiaItem item = menuDiaItemRepository.findByIdWithProducto(menuDiaItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem de menú no encontrado"));

        validarItemParaVenta(item);

        BigDecimal precio = item.getPrecioDia() != null
                ? item.getPrecioDia()
                : item.getProducto().getPrecioBase();
        precio = precio.setScale(2, RoundingMode.HALF_UP);

        CarritoItemDTO linea = CarritoItemDTO.builder()
                .productoId(item.getProducto().getId())
                .menuDiaItemId(item.getId())
                .nombre(item.getProducto().getNombre())
                .imagenUrl(item.getProducto().getImagenUrl())
                .precioUnitario(precio)
                .cantidad(cantidad)
                .sedeId(item.getMenuDia().getSede().getId())
                .build();

        CarritoDTO carrito = obtener(session);
        try {
            carrito.agregar(linea);
        } catch (IllegalStateException ex) {
            throw new BusinessException(ex.getMessage());
        }
        session.setAttribute(CarritoDTO.SESSION_KEY, carrito);
    }

    @Override
    public void actualizar(HttpSession session, Integer menuDiaItemId, int cantidad) {
        CarritoDTO carrito = obtener(session);
        carrito.actualizarCantidad(null, menuDiaItemId, cantidad);
        session.setAttribute(CarritoDTO.SESSION_KEY, carrito);
    }

    @Override
    public void eliminar(HttpSession session, Integer menuDiaItemId) {
        CarritoDTO carrito = obtener(session);
        carrito.eliminar(null, menuDiaItemId);
        session.setAttribute(CarritoDTO.SESSION_KEY, carrito);
    }

    @Override
    public void limpiar(HttpSession session) {
        CarritoDTO carrito = obtener(session);
        carrito.limpiar();
        session.setAttribute(CarritoDTO.SESSION_KEY, carrito);
    }

    private void validarItemParaVenta(MenuDiaItem item) {
        if (!"PUBLICADO".equals(item.getMenuDia().getEstado())) {
            throw new BusinessException("El menú no está publicado");
        }
        if (!LocalDate.now().equals(item.getMenuDia().getFecha())) {
            throw new BusinessException("Solo puedes pedir del menú de hoy");
        }
        if (!Boolean.TRUE.equals(item.getDisponible())) {
            throw new BusinessException("Producto agotado: " + item.getProducto().getNombre());
        }
        if (item.getCupoMaximo() != null
                && item.getCantidadVendida() != null
                && item.getCantidadVendida() >= item.getCupoMaximo()) {
            throw new BusinessException("Sin cupo para: " + item.getProducto().getNombre());
        }
    }
}
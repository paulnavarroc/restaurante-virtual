package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.ProductoDTO;
import com.saboresperu.restaurante.dto.ProductoRequestDTO;
import java.util.List;

public interface ProductoService {
    List<ProductoDTO> listarDisponibles(Integer regionId, Integer categoriaId, String busqueda);
    ProductoDTO obtenerPorId(Integer id);
    ProductoDTO crear(ProductoRequestDTO dto);
    ProductoDTO actualizar(Integer id, ProductoRequestDTO dto);
    void cambiarDisponibilidad(Integer id, boolean disponible);
}
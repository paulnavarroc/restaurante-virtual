package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.ProductoDTO;
import com.saboresperu.restaurante.dto.ProductoRequestDTO;
import java.util.List;

public interface ProductoService {
    List<ProductoDTO> listarActivos();
    ProductoDTO obtenerPorId(Integer id);
    List<ProductoDTO> listarDisponibles(Integer regionId, Integer categoriaId, String busqueda);
}
package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.ProductoDTO;
import com.saboresperu.restaurante.dto.ProductoFormDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductoAdminService {
    List<ProductoDTO> listarTodos();
    ProductoFormDTO obtenerForm(Integer id);
    Integer crear(ProductoFormDTO form);
    void actualizar(Integer id, ProductoFormDTO form);
    void desactivar(Integer id);
    void activar(Integer id);
    void actualizarImagen(Integer productoId, MultipartFile file);
}
package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.ProductoDTO;
import com.saboresperu.restaurante.entity.Producto;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.ProductoRepository;
import com.saboresperu.restaurante.service.interfaces.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarActivos() {
        return productoRepository.findByActivoTrue().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Integer id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return toDTO(p);
    }

    private ProductoDTO toDTO(Producto p) {
        return ProductoDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .precioBase(p.getPrecioBase())
                .imagenUrl(p.getImagenUrl())
                .categoria(p.getCategoria() != null ? p.getCategoria().getNombre() : null)
                .region(p.getRegion() != null ? p.getRegion().getNombre() : null)
                .stockIlimitado(p.getStockIlimitado())
                .activo(p.getActivo())
                .build();
    }
}
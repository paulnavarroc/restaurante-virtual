package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.ProductoDTO;
import com.saboresperu.restaurante.dto.ProductoRequestDTO;
import com.saboresperu.restaurante.entity.Categoria;
import com.saboresperu.restaurante.entity.Producto;
import com.saboresperu.restaurante.entity.Region;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.CategoriaRepository;
import com.saboresperu.restaurante.repository.ProductoRepository;
import com.saboresperu.restaurante.repository.RegionRepository;
import com.saboresperu.restaurante.service.interfaces.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final RegionRepository regionRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarDisponibles(Integer regionId, Integer categoriaId, String busqueda) {
        String busquedaNorm = (busqueda != null && !busqueda.isBlank()) ? busqueda.trim() : null;

        return productoRepository.buscarProductos(regionId, categoriaId, busquedaNorm)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /*
    @Override
    public List<ProductoDTO> listarDisponibles(Integer regionId, Integer categoriaId, String busqueda) {
        return productoRepository.buscarProductos(regionId, categoriaId, busqueda)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

     */
    @Override
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Integer id) {
        Producto producto = productoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return toDTO(producto);
    }

    /*
    @Override
    public ProductoDTO obtenerPorId(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return toDTO(producto);
    }

     */

    @Override
    @Transactional
    public ProductoDTO crear(ProductoRequestDTO dto) {
        Region region = regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new ResourceNotFoundException("Región no encontrada"));
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        Producto producto = Producto.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .imagenUrl(dto.getImagenUrl())
                .tiempoPreparacion(dto.getTiempoPreparacion())
                .disponible(dto.getDisponible() != null ? dto.getDisponible() : true)
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .region(region)
                .categoria(categoria)
                .build();

        return toDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoDTO actualizar(Integer id, ProductoRequestDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Region region = regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new ResourceNotFoundException("Región no encontrada"));
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setImagenUrl(dto.getImagenUrl());
        producto.setTiempoPreparacion(dto.getTiempoPreparacion());
        producto.setDisponible(dto.getDisponible());
        producto.setRegion(region);
        producto.setCategoria(categoria);

        return toDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public void cambiarDisponibilidad(Integer id, boolean disponible) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        producto.setDisponible(disponible);
        productoRepository.save(producto);
    }

    private ProductoDTO toDTO(Producto p) {
        return ProductoDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .precio(p.getPrecio())
                .imagenUrl(p.getImagenUrl())
                .tiempoPreparacion(p.getTiempoPreparacion())
                .disponible(p.getDisponible())
                .region(p.getRegion() != null ? p.getRegion().getNombre() : null)
                .categoria(p.getCategoria() != null ? p.getCategoria().getNombre() : null)
                .build();
    }
    /*
    private ProductoDTO toDTO(Producto p) {
        return ProductoDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .precio(p.getPrecio())
                .imagenUrl(p.getImagenUrl())
                .tiempoPreparacion(p.getTiempoPreparacion())
                .disponible(p.getDisponible())
                .region(p.getRegion().getNombre())
                .categoria(p.getCategoria().getNombre())
                .build();
    }

     */
}
package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.ProductoDTO;
import com.saboresperu.restaurante.dto.ProductoFormDTO;
import com.saboresperu.restaurante.entity.Categoria;
import com.saboresperu.restaurante.entity.Producto;
import com.saboresperu.restaurante.entity.Region;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.CategoriaRepository;
import com.saboresperu.restaurante.repository.ProductoRepository;
import com.saboresperu.restaurante.repository.RegionRepository;
import com.saboresperu.restaurante.service.interfaces.MediaStorageService;
import com.saboresperu.restaurante.service.interfaces.ProductoAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoAdminServiceImpl implements ProductoAdminService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final RegionRepository regionRepository;
    private final MediaStorageService mediaStorageService;


    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarTodos() {
        return productoRepository.findAllWithDetails().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoFormDTO obtenerForm(Integer id) {
        Producto p = productoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return ProductoFormDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .precioBase(p.getPrecioBase())
                .imagenUrl(p.getImagenUrl())
                .tiempoPreparacionMin(p.getTiempoPreparacionMin())
                .categoriaId(p.getCategoria().getId())
                .regionId(p.getRegion() != null ? p.getRegion().getId() : null)
                .stockIlimitado(p.getStockIlimitado())
                .activo(p.getActivo())
                .build();
    }

    @Override
    @Transactional
    public Integer crear(ProductoFormDTO form) {
        Producto p = mapNuevo(form);
        p = productoRepository.save(p);
        return p.getId();
    }

    @Override
    @Transactional
    public void actualizarImagen(Integer productoId, MultipartFile file) {
        Producto p = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        String anterior = p.getImagenUrl();
        String nuevaUrl = mediaStorageService.guardarImagenProducto(productoId, file);
        p.setImagenUrl(nuevaUrl);
        productoRepository.save(p);

        mediaStorageService.eliminarSiEsLocal(anterior);
    }

    @Override
    @Transactional
    public void actualizar(Integer id, ProductoFormDTO form) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        aplicar(p, form);
        productoRepository.save(p);
    }

    @Override
    @Transactional
    public void desactivar(Integer id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        p.setActivo(false);
        productoRepository.save(p);
    }

    @Override
    @Transactional
    public void activar(Integer id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        p.setActivo(true);
        productoRepository.save(p);
    }

    private Producto mapNuevo(ProductoFormDTO form) {
        Producto p = Producto.builder()
                .fechaRegistro(LocalDateTime.now())
                .activo(form.getActivo() != null ? form.getActivo() : true)
                .stockIlimitado(Boolean.TRUE.equals(form.getStockIlimitado()))
                .build();
        aplicar(p, form);
        return p;
    }

    private void aplicar(Producto p, ProductoFormDTO form) {
        if (form.getNombre() == null || form.getNombre().isBlank()) {
            throw new BusinessException("El nombre es obligatorio");
        }
        if (form.getPrecioBase() == null || form.getPrecioBase().signum() < 0) {
            throw new BusinessException("Precio inválido");
        }

        Categoria cat = categoriaRepository.findById(form.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        Region region = null;
        if (form.getRegionId() != null) {
            region = regionRepository.findById(form.getRegionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Región no encontrada"));
        }

        p.setNombre(form.getNombre().trim());
        p.setDescripcion(form.getDescripcion());
        p.setPrecioBase(form.getPrecioBase().setScale(2, RoundingMode.HALF_UP));
        p.setImagenUrl(blankToNull(form.getImagenUrl()));
        p.setTiempoPreparacionMin(form.getTiempoPreparacionMin());
        p.setCategoria(cat);
        p.setRegion(region);
        p.setStockIlimitado(Boolean.TRUE.equals(form.getStockIlimitado()));
        if (form.getActivo() != null) {
            p.setActivo(form.getActivo());
        }
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
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
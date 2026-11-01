package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.*;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.InsumoRepository;
import com.saboresperu.restaurante.repository.ProductoRepository;
import com.saboresperu.restaurante.repository.RecetaRepository;
import com.saboresperu.restaurante.service.interfaces.RecetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecetaServiceImpl implements RecetaService {

    private final RecetaRepository recetaRepository;
    private final ProductoRepository productoRepository;
    private final InsumoRepository insumoRepository;

    @Override
    @Transactional(readOnly = true)
    public RecetaDTO obtenerPorProducto(Integer productoId) {
        return recetaRepository.findByProductoIdWithDetalles(productoId)
                .map(this::toDTO)
                .orElse(RecetaDTO.builder()
                        .productoId(productoId)
                        .activa(true)
                        .version(1)
                        .detalles(new ArrayList<>())
                        .build());
    }

    @Override
    @Transactional
    public void guardar(Integer productoId, RecetaFormDTO form) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (Boolean.TRUE.equals(producto.getStockIlimitado())) {
            throw new BusinessException("Producto con stock ilimitado no requiere receta");
        }
        if (form.getDetalles() == null || form.getDetalles().isEmpty()) {
            throw new BusinessException("La receta debe tener al menos un insumo");
        }

        Set<Integer> vistos = new HashSet<>();
        for (RecetaDetalleFormDTO d : form.getDetalles()) {
            if (!vistos.add(d.getInsumoId())) {
                throw new BusinessException("Insumo duplicado en la receta");
            }
            if (d.getCantidad() == null || d.getCantidad().signum() <= 0) {
                throw new BusinessException("Cantidad de insumo inválida");
            }
        }

        Receta receta = recetaRepository.findByProductoId(productoId).orElse(null);
        if (receta == null) {
            receta = Receta.builder()
                    .producto(producto)
                    .version(1)
                    .activa(true)
                    .detalles(new ArrayList<>())
                    .build();
        } else {
            receta.setVersion(receta.getVersion() + 1);
            receta.getDetalles().clear();
        }
        receta.setActiva(form.getActiva() == null || form.getActiva());

        for (RecetaDetalleFormDTO d : form.getDetalles()) {
            Insumo insumo = insumoRepository.findById(d.getInsumoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));
            RecetaDetalle det = RecetaDetalle.builder()
                    .receta(receta)
                    .insumo(insumo)
                    .cantidad(d.getCantidad())
                    .build();
            receta.getDetalles().add(det);
        }
        recetaRepository.save(receta);
    }

    private RecetaDTO toDTO(Receta r) {
        return RecetaDTO.builder()
                .id(r.getId())
                .productoId(r.getProducto().getId())
                .productoNombre(r.getProducto().getNombre())
                .version(r.getVersion())
                .activa(r.getActiva())
                .detalles(r.getDetalles().stream().map(d -> RecetaDetalleDTO.builder()
                        .insumoId(d.getInsumo().getId())
                        .insumoNombre(d.getInsumo().getNombre())
                        .unidad(d.getInsumo().getUnidad())
                        .cantidad(d.getCantidad())
                        .build()).toList())
                .build();
    }
}
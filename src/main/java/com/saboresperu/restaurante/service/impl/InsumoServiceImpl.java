package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.InsumoDTO;
import com.saboresperu.restaurante.dto.InsumoFormDTO;
import com.saboresperu.restaurante.entity.Insumo;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.InsumoRepository;
import com.saboresperu.restaurante.service.interfaces.InsumoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InsumoServiceImpl implements InsumoService {

    private final InsumoRepository insumoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InsumoDTO> listarTodos() {
        return insumoRepository.findAllByOrderByNombreAsc().stream()
                .map(i -> InsumoDTO.builder()
                        .id(i.getId())
                        .nombre(i.getNombre())
                        .unidad(i.getUnidad())
                        .stockIlimitado(i.getStockIlimitado())
                        .stockMinimoAlerta(i.getStockMinimoAlerta())
                        .activo(i.getActivo())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InsumoFormDTO obtenerForm(Integer id) {
        Insumo i = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));
        return InsumoFormDTO.builder()
                .id(i.getId())
                .nombre(i.getNombre())
                .unidad(i.getUnidad())
                .stockIlimitado(i.getStockIlimitado())
                .stockMinimoAlerta(i.getStockMinimoAlerta())
                .activo(i.getActivo())
                .build();
    }

    @Override
    @Transactional
    public Integer crear(InsumoFormDTO form) {
        if (insumoRepository.existsByNombreIgnoreCase(form.getNombre().trim())) {
            throw new BusinessException("Ya existe un insumo con ese nombre");
        }
        Insumo i = Insumo.builder()
                .nombre(form.getNombre().trim())
                .unidad(form.getUnidad().trim())
                .stockIlimitado(Boolean.TRUE.equals(form.getStockIlimitado()))
                .stockMinimoAlerta(form.getStockMinimoAlerta())
                .activo(form.getActivo() == null || form.getActivo())
                .build();
        return insumoRepository.save(i).getId();
    }

    @Override
    @Transactional
    public void actualizar(Integer id, InsumoFormDTO form) {
        Insumo i = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));
        i.setNombre(form.getNombre().trim());
        i.setUnidad(form.getUnidad().trim());
        i.setStockIlimitado(Boolean.TRUE.equals(form.getStockIlimitado()));
        i.setStockMinimoAlerta(form.getStockMinimoAlerta());
        if (form.getActivo() != null) {
            i.setActivo(form.getActivo());
        }
        insumoRepository.save(i);
    }
}
package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.SedeDTO;
import com.saboresperu.restaurante.entity.Sede;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.SedeRepository;
import com.saboresperu.restaurante.service.interfaces.SedeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SedeServiceImpl implements SedeService {

    private final SedeRepository sedeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SedeDTO> listarActivas() {
        return sedeRepository.findByActivaTrue().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SedeDTO obtenerPorId(Integer id) {
        return toDTO(sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada")));
    }

    private SedeDTO toDTO(Sede s) {
        return SedeDTO.builder()
                .id(s.getId())
                .nombre(s.getNombre())
                .direccion(s.getDireccion())
                .lat(s.getLat())
                .lng(s.getLng())
                .radioCoberturaKm(s.getRadioCoberturaKm())
                .activa(s.getActiva())
                .build();
    }
}
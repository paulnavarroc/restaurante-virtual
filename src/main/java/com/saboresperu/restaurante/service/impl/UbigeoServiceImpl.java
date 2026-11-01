package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.UbigeoOptionDTO;
import com.saboresperu.restaurante.repository.UbigeoDepartamentoRepository;
import com.saboresperu.restaurante.repository.UbigeoDistritoRepository;
import com.saboresperu.restaurante.repository.UbigeoProvinciaRepository;
import com.saboresperu.restaurante.service.interfaces.UbigeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UbigeoServiceImpl implements UbigeoService {

    private final UbigeoDepartamentoRepository departamentoRepository;
    private final UbigeoProvinciaRepository provinciaRepository;
    private final UbigeoDistritoRepository distritoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UbigeoOptionDTO> listarDepartamentos() {
        return departamentoRepository.findAll().stream()
                .map(d -> UbigeoOptionDTO.builder().id(d.getId()).nombre(d.getNombre()).build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UbigeoOptionDTO> listarProvincias(String idDepartamento) {
        return provinciaRepository.findByDepartamentoIdOrderByNombreAsc(idDepartamento).stream()
                .map(p -> UbigeoOptionDTO.builder().id(p.getId()).nombre(p.getNombre()).build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UbigeoOptionDTO> listarDistritos(String idProvincia) {
        return distritoRepository.findByProvinciaIdOrderByNombreAsc(idProvincia).stream()
                .map(d -> UbigeoOptionDTO.builder().id(d.getId()).nombre(d.getNombre()).build())
                .toList();
    }
}
package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.DireccionDTO;
import com.saboresperu.restaurante.dto.DireccionFormDTO;
import com.saboresperu.restaurante.entity.Cliente;
import com.saboresperu.restaurante.entity.DireccionCliente;
import com.saboresperu.restaurante.entity.UbigeoDistrito;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.ClienteRepository;
import com.saboresperu.restaurante.repository.DireccionClienteRepository;
import com.saboresperu.restaurante.repository.UbigeoDistritoRepository;
import com.saboresperu.restaurante.service.interfaces.DireccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DireccionServiceImpl implements DireccionService {

    private final DireccionClienteRepository direccionClienteRepository;
    private final ClienteRepository clienteRepository;
    private final UbigeoDistritoRepository ubigeoDistritoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DireccionDTO> listarPorCliente(Integer clienteId) {
        return direccionClienteRepository
                .findByClienteIdAndActivaTrueOrderByEsPrincipalDesc(clienteId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void crear(Integer clienteId, DireccionFormDTO form) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        UbigeoDistrito distrito = ubigeoDistritoRepository.findById(form.getIdDistrito())
                .orElseThrow(() -> new ResourceNotFoundException("Distrito no encontrado"));

        // Validar coherencia provincia/departamento
        if (!distrito.getProvincia().getId().equals(form.getIdProvincia())) {
            throw new BusinessException("El distrito no pertenece a la provincia seleccionada");
        }
        if (!distrito.getProvincia().getDepartamento().getId().equals(form.getIdDepartamento())) {
            throw new BusinessException("La provincia no pertenece al departamento seleccionado");
        }

        boolean principal = Boolean.TRUE.equals(form.getEsPrincipal());
        if (principal) {
            quitarPrincipalActual(clienteId);
        } else {
            // primera dirección → principal
            long count = direccionClienteRepository
                    .findByClienteIdAndActivaTrueOrderByEsPrincipalDesc(clienteId).size();
            if (count == 0) {
                principal = true;
            }
        }

        // Si no hay lat/lng, usar centro del distrito
        var lat = form.getLat() != null ? form.getLat() : distrito.getLatCentro();
        var lng = form.getLng() != null ? form.getLng() : distrito.getLngCentro();

        DireccionCliente d = DireccionCliente.builder()
                .cliente(cliente)
                .distrito(distrito)
                .alias(form.getAlias())
                .urbanizacion(form.getUrbanizacion())
                .calle(form.getCalle().trim())
                .numero(form.getNumero())
                .referencia(form.getReferencia())
                .lat(lat)
                .lng(lng)
                .esPrincipal(principal)
                .activa(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        direccionClienteRepository.save(d);
    }

    @Override
    @Transactional
    public void marcarPrincipal(Integer direccionId, Integer clienteId) {
        DireccionCliente d = direccionClienteRepository
                .findByIdAndClienteIdAndActivaTrue(direccionId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));
        quitarPrincipalActual(clienteId);
        d.setEsPrincipal(true);
        direccionClienteRepository.save(d);
    }

    @Override
    @Transactional
    public void eliminar(Integer direccionId, Integer clienteId) {
        DireccionCliente d = direccionClienteRepository
                .findByIdAndClienteIdAndActivaTrue(direccionId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));
        d.setActiva(false);
        d.setEsPrincipal(false);
        direccionClienteRepository.save(d);
    }

    private void quitarPrincipalActual(Integer clienteId) {
        direccionClienteRepository.findByClienteIdAndActivaTrueOrderByEsPrincipalDesc(clienteId)
                .forEach(x -> {
                    if (Boolean.TRUE.equals(x.getEsPrincipal())) {
                        x.setEsPrincipal(false);
                        direccionClienteRepository.save(x);
                    }
                });
    }

    private DireccionDTO toDTO(DireccionCliente d) {
        String distrito = d.getDistrito() != null ? d.getDistrito().getNombre() : "";
        String resumen = (d.getAlias() != null ? d.getAlias() + " – " : "")
                + d.getCalle()
                + (d.getNumero() != null ? " " + d.getNumero() : "")
                + ", " + distrito;
        return DireccionDTO.builder()
                .id(d.getId())
                .alias(d.getAlias())
                .calle(d.getCalle())
                .numero(d.getNumero())
                .distrito(distrito)
                .esPrincipal(d.getEsPrincipal())
                .resumen(resumen)
                .build();
    }
}
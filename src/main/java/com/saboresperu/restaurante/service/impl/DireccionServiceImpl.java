package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.DireccionDTO;
import com.saboresperu.restaurante.entity.Cliente;
import com.saboresperu.restaurante.entity.Direccion;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.ClienteRepository;
import com.saboresperu.restaurante.repository.DireccionRepository;
import com.saboresperu.restaurante.service.interfaces.DireccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DireccionServiceImpl implements DireccionService {

    private final DireccionRepository direccionRepository;
    private final ClienteRepository clienteRepository;

    @Override
    public List<DireccionDTO> listarPorCliente(Integer clienteId) {
        return direccionRepository.findByClienteIdAndActivoTrue(clienteId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DireccionDTO obtenerPorId(Integer direccionId, Integer clienteId) {
        Direccion direccion = buscarYValidar(direccionId, clienteId);
        return toDTO(direccion);
    }

    @Override
    @Transactional
    public DireccionDTO crear(Integer clienteId, DireccionDTO dto) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        // Si es la primera dirección, se marca como principal
        boolean esPrimera = direccionRepository.findByClienteIdAndActivoTrue(clienteId).isEmpty();

        Direccion direccion = Direccion.builder()
                .cliente(cliente)
                .alias(dto.getAlias())
                .direccionCompleta(dto.getDireccionCompleta())
                .referencia(dto.getReferencia())
                .distrito(dto.getDistrito())
                .provincia(dto.getProvincia() != null ? dto.getProvincia() : "Lima")
                .departamento(dto.getDepartamento() != null ? dto.getDepartamento() : "Lima")
                .esPrincipal(esPrimera || Boolean.TRUE.equals(dto.getEsPrincipal()))
                .activo(true)
                .build();

        // Si se marca como principal, desmarcar las demás
        if (Boolean.TRUE.equals(direccion.getEsPrincipal())) {
            desmarcarPrincipales(clienteId);
        }

        return toDTO(direccionRepository.save(direccion));
    }

    @Override
    @Transactional
    public DireccionDTO actualizar(Integer direccionId, Integer clienteId, DireccionDTO dto) {
        Direccion direccion = buscarYValidar(direccionId, clienteId);

        direccion.setAlias(dto.getAlias());
        direccion.setDireccionCompleta(dto.getDireccionCompleta());
        direccion.setReferencia(dto.getReferencia());
        direccion.setDistrito(dto.getDistrito());
        direccion.setProvincia(dto.getProvincia());
        direccion.setDepartamento(dto.getDepartamento());

        if (Boolean.TRUE.equals(dto.getEsPrincipal()) && !Boolean.TRUE.equals(direccion.getEsPrincipal())) {
            desmarcarPrincipales(clienteId);
            direccion.setEsPrincipal(true);
        }

        return toDTO(direccionRepository.save(direccion));
    }

    @Override
    @Transactional
    public void eliminar(Integer direccionId, Integer clienteId) {
        Direccion direccion = buscarYValidar(direccionId, clienteId);

        // Soft delete
        direccion.setActivo(false);
        direccion.setEsPrincipal(false);
        direccionRepository.save(direccion);

        // Si era la principal, marcar otra como principal
        List<Direccion> activas = direccionRepository.findByClienteIdAndActivoTrue(clienteId);
        if (!activas.isEmpty() && activas.stream().noneMatch(d -> Boolean.TRUE.equals(d.getEsPrincipal()))) {
            Direccion nuevaPrincipal = activas.get(0);
            nuevaPrincipal.setEsPrincipal(true);
            direccionRepository.save(nuevaPrincipal);
        }
    }

    @Override
    @Transactional
    public void marcarComoPrincipal(Integer direccionId, Integer clienteId) {
        Direccion direccion = buscarYValidar(direccionId, clienteId);
        desmarcarPrincipales(clienteId);
        direccion.setEsPrincipal(true);
        direccionRepository.save(direccion);
    }

    // ========== Métodos privados ==========

    private Direccion buscarYValidar(Integer direccionId, Integer clienteId) {
        Direccion direccion = direccionRepository.findById(direccionId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

        if (!direccion.getCliente().getId().equals(clienteId)) {
            throw new BusinessException("La dirección no pertenece al cliente");
        }

        if (!Boolean.TRUE.equals(direccion.getActivo())) {
            throw new BusinessException("La dirección está inactiva");
        }

        return direccion;
    }

    private void desmarcarPrincipales(Integer clienteId) {
        List<Direccion> direcciones = direccionRepository.findByClienteIdAndActivoTrue(clienteId);
        for (Direccion d : direcciones) {
            if (Boolean.TRUE.equals(d.getEsPrincipal())) {
                d.setEsPrincipal(false);
                direccionRepository.save(d);
            }
        }
    }

    private DireccionDTO toDTO(Direccion d) {
        return DireccionDTO.builder()
                .id(d.getId())
                .alias(d.getAlias())
                .direccionCompleta(d.getDireccionCompleta())
                .referencia(d.getReferencia())
                .distrito(d.getDistrito())
                .provincia(d.getProvincia())
                .departamento(d.getDepartamento())
                .esPrincipal(d.getEsPrincipal())
                .build();
    }
}
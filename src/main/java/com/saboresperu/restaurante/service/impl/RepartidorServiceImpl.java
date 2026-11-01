package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.EstadosRepartidor;
import com.saboresperu.restaurante.entity.Repartidor;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.RepartidorRepository;
import com.saboresperu.restaurante.service.interfaces.RepartidorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RepartidorServiceImpl implements RepartidorService {

    private final RepartidorRepository repartidorRepository;

    @Override
    @Transactional
    public void ponerDisponible(Integer usuarioId) {
        Repartidor r = repartidorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Repartidor no encontrado"));
        if (EstadosRepartidor.EN_ENTREGA.equals(r.getEstado())) {
            throw new BusinessException("Termina la entrega en curso primero");
        }
        r.setEstado(EstadosRepartidor.DISPONIBLE);
        repartidorRepository.save(r);
    }

    @Override
    @Transactional
    public void ponerOffline(Integer usuarioId) {
        Repartidor r = repartidorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Repartidor no encontrado"));
        if (EstadosRepartidor.EN_ENTREGA.equals(r.getEstado())) {
            throw new BusinessException("No puedes salir offline con entrega activa");
        }
        r.setEstado(EstadosRepartidor.OFFLINE);
        repartidorRepository.save(r);
    }
}

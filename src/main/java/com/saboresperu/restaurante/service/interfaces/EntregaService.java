package com.saboresperu.restaurante.service.interfaces;

import com.saboresperu.restaurante.dto.EntregaDTO;
import com.saboresperu.restaurante.entity.Pedido;

import java.util.List;

public interface EntregaService {

    /** Crea entrega PENDIENTE e intenta asignar repartidor disponible de la sede. */
    void crearYAsignarAlEstarListo(Pedido pedido);

    List<EntregaDTO> listarMisEntregas(Integer usuarioId);

    EntregaDTO tomarPendiente(Integer entregaId, Integer usuarioId);

    EntregaDTO marcarEnCamino(Integer entregaId, Integer usuarioId);

    EntregaDTO marcarEntregada(Integer entregaId, Integer usuarioId);
}
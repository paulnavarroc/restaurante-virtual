package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.EstadosEntrega;
import com.saboresperu.restaurante.domain.EstadosRepartidor;
import com.saboresperu.restaurante.dto.DetallePedidoResponseDTO;
import com.saboresperu.restaurante.dto.EntregaDTO;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.EntregaRepository;
import com.saboresperu.restaurante.repository.EstadoPedidoRepository;
import com.saboresperu.restaurante.repository.HistorialPedidoRepository;
import com.saboresperu.restaurante.repository.PedidoRepository;
import com.saboresperu.restaurante.repository.RepartidorRepository;
import com.saboresperu.restaurante.service.interfaces.EntregaService;
import com.saboresperu.restaurante.service.interfaces.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EntregaServiceImpl implements EntregaService {

    private final EntregaRepository entregaRepository;
    private final RepartidorRepository repartidorRepository;
    private final PedidoRepository pedidoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final HistorialPedidoRepository historialPedidoRepository;
    private final GeoService geoService;

    @Override
    @Transactional
    public void crearYAsignarAlEstarListo(Pedido pedido) {
        if (entregaRepository.findByPedidoId(pedido.getId()).isPresent()) {
            return; // idempotente
        }

        Entrega entrega = Entrega.builder()
                .pedido(pedido)
                .estado(EstadosEntrega.PENDIENTE_ASIGNACION)
                .build();
        entrega = entregaRepository.save(entrega);

        List<Repartidor> disponibles = repartidorRepository.findDisponiblesBySede(
                pedido.getSede().getId(), EstadosRepartidor.DISPONIBLE);

        if (disponibles.isEmpty()) {
            return; // queda pendiente para toma manual
        }

        Repartidor elegido = elegirPorProximidad(disponibles, pedido)
                .orElse(disponibles.get(0));

        asignar(entrega, elegido);
    }

    private java.util.Optional<Repartidor> elegirPorProximidad(List<Repartidor> lista, Pedido pedido) {
        var dir = pedido.getDireccion();
        if (dir == null) {
            return java.util.Optional.empty();
        }
        var lat = dir.getLat() != null ? dir.getLat()
                : (dir.getDistrito() != null ? dir.getDistrito().getLatCentro() : null);
        var lng = dir.getLng() != null ? dir.getLng()
                : (dir.getDistrito() != null ? dir.getDistrito().getLngCentro() : null);
        if (lat == null || lng == null) {
            return java.util.Optional.empty();
        }

        return lista.stream()
                .filter(r -> r.getUltimaLat() != null && r.getUltimaLng() != null)
                .min(Comparator.comparingDouble(r ->
                        geoService.distanciaKm(
                                lat.doubleValue(), lng.doubleValue(),
                                r.getUltimaLat().doubleValue(),
                                r.getUltimaLng().doubleValue())))
                .or(() -> java.util.Optional.of(lista.get(0)));
    }

    private void asignar(Entrega entrega, Repartidor repartidor) {
        entrega.setRepartidor(repartidor);
        entrega.setEstado(EstadosEntrega.ASIGNADA);
        entrega.setFechaAsignacion(LocalDateTime.now());
        entregaRepository.save(entrega);

        repartidor.setEstado(EstadosRepartidor.EN_ENTREGA);
        repartidorRepository.save(repartidor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntregaDTO> listarMisEntregas(Integer usuarioId) {
        Repartidor r = repartidorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No eres repartidor"));

        List<Entrega> mias = entregaRepository.findByRepartidorAndEstados(
                r.getId(),
                List.of(EstadosEntrega.ASIGNADA, EstadosEntrega.EN_CAMINO,
                        EstadosEntrega.PENDIENTE_ASIGNACION));

        // Pendientes de la sede sin repartidor (para "tomar")
        List<Entrega> pendientesSede = entregaRepository.findPendientesBySede(
                r.getSede().getId(), EstadosEntrega.PENDIENTE_ASIGNACION);

        return java.util.stream.Stream.concat(mias.stream(), pendientesSede.stream())
                .distinct()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public EntregaDTO tomarPendiente(Integer entregaId, Integer usuarioId) {
        Repartidor r = repartidorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No eres repartidor"));

        if (!EstadosRepartidor.DISPONIBLE.equals(r.getEstado())
                && !EstadosRepartidor.EN_ENTREGA.equals(r.getEstado())) {
            // permitir tomar si está disponible; si ya en entrega, política: solo 1 a la vez
        }
        if (EstadosRepartidor.EN_ENTREGA.equals(r.getEstado())) {
            throw new BusinessException("Ya tienes una entrega en curso");
        }
        if (EstadosRepartidor.OFFLINE.equals(r.getEstado())) {
            throw new BusinessException("Ponte disponible antes de tomar entregas");
        }

        Entrega e = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada"));

        if (!EstadosEntrega.PENDIENTE_ASIGNACION.equals(e.getEstado())) {
            throw new BusinessException("La entrega ya no está pendiente");
        }
        if (!e.getPedido().getSede().getId().equals(r.getSede().getId())) {
            throw new BusinessException("Entrega de otra sede");
        }

        asignar(e, r);
        return toDTO(e);
    }

    @Override
    @Transactional
    public EntregaDTO marcarEnCamino(Integer entregaId, Integer usuarioId) {
        Entrega e = obtenerDeRepartidor(entregaId, usuarioId);
        if (!EstadosEntrega.ASIGNADA.equals(e.getEstado())) {
            throw new BusinessException("La entrega debe estar ASIGNADA");
        }
        e.setEstado(EstadosEntrega.EN_CAMINO);
        e.setFechaRecojo(LocalDateTime.now());
        entregaRepository.save(e);

        // Sincronizar estado del pedido
        cambiarEstadoPedido(e.getPedido(), "EN_CAMINO", usuarioId, "Repartidor en camino");
        return toDTO(e);
    }

    @Override
    @Transactional
    public EntregaDTO marcarEntregada(Integer entregaId, Integer usuarioId) {
        Entrega e = obtenerDeRepartidor(entregaId, usuarioId);
        if (!EstadosEntrega.EN_CAMINO.equals(e.getEstado())
                && !EstadosEntrega.ASIGNADA.equals(e.getEstado())) {
            throw new BusinessException("Estado de entrega inválido para completar");
        }
        e.setEstado(EstadosEntrega.ENTREGADA);
        e.setFechaEntrega(LocalDateTime.now());
        entregaRepository.save(e);

        cambiarEstadoPedido(e.getPedido(), "ENTREGADO", usuarioId, "Pedido entregado");

        Repartidor r = e.getRepartidor();
        r.setEstado(EstadosRepartidor.DISPONIBLE);
        repartidorRepository.save(r);

        return toDTO(e);
    }

    private Entrega obtenerDeRepartidor(Integer entregaId, Integer usuarioId) {
        Repartidor r = repartidorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No eres repartidor"));
        Entrega e = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada"));
        if (e.getRepartidor() == null || !e.getRepartidor().getId().equals(r.getId())) {
            throw new BusinessException("Esta entrega no te está asignada");
        }
        return e;
    }

    private void cambiarEstadoPedido(Pedido pedido, String nombreEstado, Integer usuarioId, String obs) {
        EstadoPedido estado = estadoPedidoRepository.findByNombre(nombreEstado)
                .orElseThrow(() -> new BusinessException("Estado " + nombreEstado + " no existe"));
        pedido.setEstadoPedido(estado);
        pedidoRepository.save(pedido);
        historialPedidoRepository.save(HistorialPedido.builder()
                .pedido(pedido)
                .estadoPedido(estado)
                .observacion(obs)
                .fechaCambio(LocalDateTime.now())
                .build());
    }

    private EntregaDTO toDTO(Entrega e) {
        Pedido p = e.getPedido();
        String clienteNombre = "";
        if (p.getCliente() != null && p.getCliente().getUsuario() != null) {
            var u = p.getCliente().getUsuario();
            clienteNombre = u.getNombre() + " " + u.getApellido();
        }
        String dir = p.getDireccion() != null ? p.getDireccion().getCalle() : "";

        List<DetallePedidoResponseDTO> items = p.getDetalles() == null ? List.of()
                : p.getDetalles().stream().map(d -> DetallePedidoResponseDTO.builder()
                .productoId(d.getProducto().getId())
                .nombreProducto(d.getProducto().getNombre())
                .cantidad(d.getCantidad())
                .precioUnitarioFinal(d.getPrecioUnitarioFinal())
                .subtotal(d.getSubtotal())
                .build()).toList();

        String repNombre = null;
        if (e.getRepartidor() != null && e.getRepartidor().getUsuario() != null) {
            var u = e.getRepartidor().getUsuario();
            repNombre = u.getNombre() + " " + u.getApellido();
        }

        return EntregaDTO.builder()
                .id(e.getId())
                .pedidoId(p.getId())
                .estadoPedido(p.getEstadoPedido() != null ? p.getEstadoPedido().getNombre() : null)
                .estadoEntrega(e.getEstado())
                .repartidorId(e.getRepartidor() != null ? e.getRepartidor().getId() : null)
                .repartidorNombre(repNombre)
                .direccion(dir)
                .clienteNombre(clienteNombre)
                .total(p.getTotal())
                .fechaPedido(p.getFechaPedido())
                .fechaAsignacion(e.getFechaAsignacion())
                .items(items)
                .build();
    }
}
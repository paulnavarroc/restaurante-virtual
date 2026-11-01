package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.*;
import com.saboresperu.restaurante.entity.Pedido;
import com.saboresperu.restaurante.entity.Sede;
import com.saboresperu.restaurante.repository.PedidoRepository;
import com.saboresperu.restaurante.repository.SedeRepository;
import com.saboresperu.restaurante.service.interfaces.DashboardService;
import com.saboresperu.restaurante.service.interfaces.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final PedidoRepository pedidoRepository;
    private final SedeRepository sedeRepository;
    private final StockService stockService;

    @Override
    @Transactional(readOnly = true)
    public DashboardDTO obtener(Integer sedeId) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime desde = hoy.atStartOfDay();
        LocalDateTime hasta = hoy.plusDays(1).atStartOfDay();

        String sedeNombre = null;
        if (sedeId != null) {
            sedeNombre = sedeRepository.findById(sedeId)
                    .map(Sede::getNombre)
                    .orElse(null);
        }

        long pedidosHoy = pedidoRepository.countPedidosEntre(desde, hasta, sedeId);
        BigDecimal ventasHoy = pedidoRepository.sumVentasEntre(desde, hasta, sedeId);
        if (ventasHoy == null) {
            ventasHoy = BigDecimal.ZERO;
        }

        long pendientesCocina = pedidoRepository.countByEstados(
                sedeId,
                List.of("PENDIENTE", "CONFIRMADO", "EN_PREPARACION", "LISTO")
        );
        long enCamino = pedidoRepository.countByEstados(sedeId, List.of("EN_CAMINO"));

        List<EstadoCountDTO> porEstado = pedidoRepository
                .countAgrupadoPorEstadoHoy(desde, hasta, sedeId)
                .stream()
                .map(row -> EstadoCountDTO.builder()
                        .estado((String) row[0])
                        .cantidad((Long) row[1])
                        .build())
                .toList();

        List<Pedido> recientes = pedidoRepository.findRecientes(
                desde.minusDays(7), sedeId, PageRequest.of(0, 10));

        List<PedidoResponseDTO> ultimos = recientes.stream()
                .map(this::toPedidoResumen)
                .toList();

        List<StockSedeDTO> stockBajo = List.of();
        if (sedeId != null) {
            stockBajo = stockService.listarStockPorSede(sedeId).stream()
                    .filter(StockSedeDTO::isAlertaMinimo)
                    .limit(15)
                    .toList();
        } else {
            // todas las sedes activas
            stockBajo = sedeRepository.findByActivaTrue().stream()
                    .flatMap(s -> stockService.listarStockPorSede(s.getId()).stream())
                    .filter(StockSedeDTO::isAlertaMinimo)
                    .limit(15)
                    .toList();
        }

        return DashboardDTO.builder()
                .pedidosHoy(pedidosHoy)
                .pedidosPendientesCocina(pendientesCocina)
                .pedidosEnCamino(enCamino)
                .ventasHoy(ventasHoy)
                .alertasStock(stockBajo.size())
                .sedeIdFiltro(sedeId)
                .sedeNombre(sedeNombre)
                .ultimosPedidos(ultimos)
                .stockBajo(stockBajo)
                .pedidosPorEstado(porEstado)
                .build();
    }

    private PedidoResponseDTO toPedidoResumen(Pedido p) {
        String cliente = "";
        if (p.getCliente() != null && p.getCliente().getUsuario() != null) {
            var u = p.getCliente().getUsuario();
            cliente = u.getNombre() + " " + u.getApellido();
        }
        return PedidoResponseDTO.builder()
                .id(p.getId())
                .estado(p.getEstadoPedido() != null ? p.getEstadoPedido().getNombre() : null)
                .fechaPedido(p.getFechaPedido())
                .sedeId(p.getSede() != null ? p.getSede().getId() : null)
                .sedeNombre(p.getSede() != null ? p.getSede().getNombre() : null)
                .total(p.getTotal())
                .observaciones(cliente) // reuso temporal o añade campo clienteNombre al DTO
                .build();
    }
}
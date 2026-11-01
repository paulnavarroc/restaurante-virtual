package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.EstadosReservaStock;
import com.saboresperu.restaurante.domain.TiposMovimientoStock;
import com.saboresperu.restaurante.dto.EntradaStockRequestDTO;
import com.saboresperu.restaurante.dto.StockSedeDTO;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockInsumoRepository stockInsumoRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final ReservaStockPedidoRepository reservaStockPedidoRepository;
    private final RecetaRepository recetaRepository;
    private final InsumoRepository insumoRepository;
    private final SedeRepository sedeRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public void registrarEntrada(EntradaStockRequestDTO request, Integer usuarioId) {
        if (request.getCantidad() == null || request.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Cantidad inválida");
        }

        Sede sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
        Insumo insumo = insumoRepository.findById(request.getInsumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));

        if (Boolean.TRUE.equals(insumo.getStockIlimitado())) {
            throw new BusinessException("El insumo es ilimitado; no requiere entrada de stock");
        }

        StockInsumo stock = stockInsumoRepository
                .findBySedeIdAndInsumoId(sede.getId(), insumo.getId())
                .orElseGet(() -> StockInsumo.builder()
                        .sede(sede)
                        .insumo(insumo)
                        .cantidadDisponible(BigDecimal.ZERO)
                        .cantidadReservada(BigDecimal.ZERO)
                        .version(0)
                        .build());

        stock.setCantidadDisponible(stock.getCantidadDisponible().add(request.getCantidad()));
        if (request.getCostoUnitario() != null) {
            stock.setUltimoCosto(request.getCostoUnitario());
            // promedio simple ponderado
            // (opcional: mejorar después)
            stock.setCostoPromedio(request.getCostoUnitario());
        }
        stockInsumoRepository.save(stock);

        MovimientoStock mov = MovimientoStock.builder()
                .sede(sede)
                .insumo(insumo)
                .tipo(TiposMovimientoStock.ENTRADA)
                .cantidad(request.getCantidad())
                .costoUnitario(request.getCostoUnitario())
                .usuario(usuarioId != null ? usuarioRepository.findById(usuarioId).orElse(null) : null)
                .observacion(request.getObservacion())
                .fecha(LocalDateTime.now())
                .build();
        movimientoStockRepository.save(mov);
    }

    @Override
    @Transactional
    public void reservarParaPedido(Pedido pedido, Map<Integer, Integer> productoCantidades) {
        Integer sedeId = pedido.getSede().getId();
        Map<Integer, BigDecimal> insumosNecesarios = new HashMap<>();

        for (Map.Entry<Integer, Integer> e : productoCantidades.entrySet()) {
            Integer productoId = e.getKey();
            int cantPlatos = e.getValue();

            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto " + productoId));

            if (Boolean.TRUE.equals(producto.getStockIlimitado())) {
                continue; // no descuenta insumos
            }

            Receta receta = recetaRepository.findByProductoIdAndActivaTrue(productoId)
                    .orElseThrow(() -> new BusinessException(
                            "El producto no tiene receta activa: " + producto.getNombre()));

            for (RecetaDetalle det : receta.getDetalles()) {
                if (Boolean.TRUE.equals(det.getInsumo().getStockIlimitado())) {
                    continue;
                }
                BigDecimal need = det.getCantidad()
                        .multiply(BigDecimal.valueOf(cantPlatos));
                insumosNecesarios.merge(det.getInsumo().getId(), need, BigDecimal::add);
            }
        }

        for (Map.Entry<Integer, BigDecimal> e : insumosNecesarios.entrySet()) {
            Integer insumoId = e.getKey();
            BigDecimal need = e.getValue().setScale(4, RoundingMode.HALF_UP);

            StockInsumo stock = stockInsumoRepository
                    .findBySedeIdAndInsumoId(sedeId, insumoId)
                    .orElseThrow(() -> new BusinessException(
                            "Sin stock configurado para insumo id=" + insumoId));

            BigDecimal disponibleNeto = stock.getCantidadDisponible()
                    .subtract(stock.getCantidadReservada());

            if (disponibleNeto.compareTo(need) < 0) {
                String nombre = stock.getInsumo().getNombre();
                throw new BusinessException(
                        "Stock insuficiente de '" + nombre + "'. Disponible: "
                                + disponibleNeto + ", requerido: " + need);
            }

            stock.setCantidadReservada(stock.getCantidadReservada().add(need));
            stockInsumoRepository.save(stock); // @Version controla concurrencia

            reservaStockPedidoRepository.save(ReservaStockPedido.builder()
                    .pedido(pedido)
                    .sede(pedido.getSede())
                    .insumo(stock.getInsumo())
                    .cantidad(need)
                    .estado(EstadosReservaStock.RESERVADA)
                    .build());

            movimientoStockRepository.save(MovimientoStock.builder()
                    .sede(pedido.getSede())
                    .insumo(stock.getInsumo())
                    .tipo(TiposMovimientoStock.RESERVA)
                    .cantidad(need)
                    .pedido(pedido)
                    .fecha(LocalDateTime.now())
                    .build());
        }
    }

    @Override
    @Transactional
    public void consumirReservas(Integer pedidoId) {
        List<ReservaStockPedido> reservas = reservaStockPedidoRepository
                .findByPedidoIdAndEstado(pedidoId, EstadosReservaStock.RESERVADA);

        for (ReservaStockPedido r : reservas) {
            StockInsumo stock = stockInsumoRepository
                    .findBySedeIdAndInsumoId(r.getSede().getId(), r.getInsumo().getId())
                    .orElseThrow();

            stock.setCantidadReservada(stock.getCantidadReservada().subtract(r.getCantidad()));
            stock.setCantidadDisponible(stock.getCantidadDisponible().subtract(r.getCantidad()));
            stockInsumoRepository.save(stock);

            r.setEstado(EstadosReservaStock.CONSUMIDA);
            reservaStockPedidoRepository.save(r);

            movimientoStockRepository.save(MovimientoStock.builder()
                    .sede(r.getSede())
                    .insumo(r.getInsumo())
                    .tipo(TiposMovimientoStock.CONSUMO)
                    .cantidad(r.getCantidad())
                    .pedido(r.getPedido())
                    .fecha(LocalDateTime.now())
                    .build());
        }
    }

    @Override
    @Transactional
    public void liberarReservas(Integer pedidoId) {
        List<ReservaStockPedido> reservas = reservaStockPedidoRepository
                .findByPedidoIdAndEstado(pedidoId, EstadosReservaStock.RESERVADA);

        for (ReservaStockPedido r : reservas) {
            StockInsumo stock = stockInsumoRepository
                    .findBySedeIdAndInsumoId(r.getSede().getId(), r.getInsumo().getId())
                    .orElseThrow();

            stock.setCantidadReservada(stock.getCantidadReservada().subtract(r.getCantidad()));
            stockInsumoRepository.save(stock);

            r.setEstado(EstadosReservaStock.LIBERADA);
            reservaStockPedidoRepository.save(r);

            movimientoStockRepository.save(MovimientoStock.builder()
                    .sede(r.getSede())
                    .insumo(r.getInsumo())
                    .tipo(TiposMovimientoStock.LIBERACION)
                    .cantidad(r.getCantidad())
                    .pedido(r.getPedido())
                    .fecha(LocalDateTime.now())
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockSedeDTO> listarStockPorSede(Integer sedeId) {
        return stockInsumoRepository.findBySedeIdWithInsumo(sedeId).stream()
                .map(s -> {
                    BigDecimal neto = s.getCantidadDisponible().subtract(s.getCantidadReservada());
                    BigDecimal minimo = s.getStockMinimoAlerta() != null
                            ? s.getStockMinimoAlerta()
                            : s.getInsumo().getStockMinimoAlerta();
                    boolean alerta = minimo != null && neto.compareTo(minimo) <= 0
                            && !Boolean.TRUE.equals(s.getInsumo().getStockIlimitado());
                    return StockSedeDTO.builder()
                            .stockId(s.getId())
                            .sedeId(s.getSede().getId())
                            .sedeNombre(s.getSede().getNombre())
                            .insumoId(s.getInsumo().getId())
                            .insumoNombre(s.getInsumo().getNombre())
                            .unidad(s.getInsumo().getUnidad())
                            .cantidadDisponible(s.getCantidadDisponible())
                            .cantidadReservada(s.getCantidadReservada())
                            .disponibleNeto(neto)
                            .ultimoCosto(s.getUltimoCosto())
                            .alertaMinimo(alerta)
                            .build();
                })
                .toList();
    }
}
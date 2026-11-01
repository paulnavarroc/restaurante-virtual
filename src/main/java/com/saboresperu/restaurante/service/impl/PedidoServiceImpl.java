package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.*;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.DeliveryService;
import com.saboresperu.restaurante.service.interfaces.GeoService;
import com.saboresperu.restaurante.service.interfaces.PedidoService;
import com.saboresperu.restaurante.service.interfaces.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final DireccionClienteRepository direccionClienteRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final MenuDiaItemRepository menuDiaItemRepository;
    private final ProductoRepository productoRepository;
    private final PagoRepository pagoRepository;
    private final HistorialPedidoRepository historialPedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracionSedeRepository configuracionSedeRepository;

    private final GeoService geoService;
    private final DeliveryService deliveryService;
    private final StockService stockService;

    private static final Set<String> TRANSICIONES_COCINA = Set.of(
            "PENDIENTE", "CONFIRMADO", "EN_PREPARACION", "LISTO"
    );

    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(Integer clienteId, CrearPedidoRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        DireccionCliente direccion = direccionClienteRepository
                .findByIdAndClienteIdWithDistrito(dto.getDireccionId(), clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

        Sede sede = geoService.resolverSedeParaDireccion(direccion)
                .orElseThrow(() -> new BusinessException(
                        "No hay cocina que cubra esta dirección"));

        ConfiguracionSede cfg = configuracionSedeRepository.findById(sede.getId()).orElse(null);

        EstadoPedido estadoInicial = estadoPedidoRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new ResourceNotFoundException("Estado PENDIENTE no existe"));

        MetodoPago metodoPago = metodoPagoRepository.findById(dto.getMetodoPagoId())
                .orElseThrow(() -> new ResourceNotFoundException("Método de pago no encontrado"));

        // Distancia
        BigDecimal lat = direccion.getLat() != null ? direccion.getLat()
                : direccion.getDistrito().getLatCentro();
        BigDecimal lng = direccion.getLng() != null ? direccion.getLng()
                : direccion.getDistrito().getLngCentro();

        double distKm = geoService.distanciaKm(
                lat.doubleValue(), lng.doubleValue(),
                sede.getLat().doubleValue(), sede.getLng().doubleValue());

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        List<DetallePedido> detalles = new ArrayList<>();
        Map<Integer, Integer> productoCantidades = new HashMap<>();
        Map<Integer, BigDecimal> preciosLista = new HashMap<>();
        Map<Integer, Integer> productoToMenuItem = new HashMap<>();
        BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (DetallePedidoRequestDTO item : dto.getItems()) {
            MenuDiaItem menuItem = null;
            Producto producto;

            if (item.getMenuDiaItemId() != null) {
                menuItem = menuDiaItemRepository.findByIdWithProducto(item.getMenuDiaItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ítem de menú no encontrado"));

                if (!menuItem.getMenuDia().getSede().getId().equals(sede.getId())) {
                    throw new BusinessException("El plato no pertenece a la cocina asignada");
                }
                if (!hoy.equals(menuItem.getMenuDia().getFecha())) {
                    throw new BusinessException("El menú no corresponde al día de hoy");
                }
                if (!"PUBLICADO".equals(menuItem.getMenuDia().getEstado())) {
                    throw new BusinessException("El menú del día no está publicado");
                }
                if (!Boolean.TRUE.equals(menuItem.getDisponible())) {
                    throw new BusinessException("Producto agotado: " + menuItem.getProducto().getNombre());
                }
                if (menuItem.getCupoMaximo() != null
                        && menuItem.getCantidadVendida() + item.getCantidad() > menuItem.getCupoMaximo()) {
                    throw new BusinessException("Cupo del día superado: " + menuItem.getProducto().getNombre());
                }
                // turno (si tiene turnos asociados)
                if (menuItem.getTurnos() != null && !menuItem.getTurnos().isEmpty()) {
                    boolean enTurno = menuItem.getTurnos().stream().anyMatch(t ->
                            !ahora.isBefore(t.getHoraInicio()) && ahora.isBefore(t.getHoraFin()));
                    if (!enTurno) {
                        throw new BusinessException(
                                "Fuera de horario para: " + menuItem.getProducto().getNombre());
                    }
                }

                producto = menuItem.getProducto();
            } else {
                producto = productoRepository.findById(item.getProductoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
            }

            if (!Boolean.TRUE.equals(producto.getActivo())) {
                throw new BusinessException("Producto inactivo: " + producto.getNombre());
            }

            BigDecimal precioLista = (menuItem != null && menuItem.getPrecioDia() != null)
                    ? menuItem.getPrecioDia()
                    : producto.getPrecioBase();
            precioLista = precioLista.setScale(2, RoundingMode.HALF_UP);

            // Promos: por ahora precio final = lista
            BigDecimal precioFinal = precioLista;
            BigDecimal descUnit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            BigDecimal linea = precioFinal
                    .multiply(BigDecimal.valueOf(item.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(linea);

            DetallePedido det = DetallePedido.builder()
                    .producto(producto)
                    .menuDiaItem(menuItem)
                    .cantidad(item.getCantidad())
                    .precioLista(precioLista)
                    .precioUnitarioFinal(precioFinal)
                    .descuentoUnitario(descUnit)
                    .subtotal(linea)
                    .build();
            detalles.add(det);

            productoCantidades.merge(producto.getId(), item.getCantidad(), Integer::sum);
            preciosLista.put(producto.getId(), precioLista);

            if (menuItem != null) {
                productoToMenuItem.put(producto.getId(), menuItem.getId());
                menuItem.setCantidadVendida(menuItem.getCantidadVendida() + item.getCantidad());
                menuDiaItemRepository.save(menuItem);
            }
        }

        if (cfg != null && cfg.getPedidoMinimo() != null
                && subtotal.compareTo(cfg.getPedidoMinimo()) < 0) {
            throw new BusinessException("Pedido mínimo: S/ " + cfg.getPedidoMinimo());
        }

        BigDecimal delivery = deliveryService.calcularCosto(sede, distKm, subtotal);
        BigDecimal descuentoTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(descuentoTotal).add(delivery)
                .setScale(2, RoundingMode.HALF_UP);

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .direccion(direccion)
                .sede(sede)
                .estadoPedido(estadoInicial)
                .fechaPedido(LocalDateTime.now())
                .subtotal(subtotal)
                .descuentoTotal(descuentoTotal)
                .costoDelivery(delivery)
                .total(total)
                .distanciaKm(BigDecimal.valueOf(distKm).setScale(2, RoundingMode.HALF_UP))
                .observaciones(dto.getObservaciones())
                .codigoCupon(dto.getCodigoCupon())
                .build();

        for (DetallePedido d : detalles) {
            d.setPedido(pedido);
        }
        pedido.setDetalles(detalles);

        pedido = pedidoRepository.save(pedido);

        // Reserva de insumos (falla toda la tx si no hay stock)
        stockService.reservarParaPedido(pedido, productoCantidades);

        pagoRepository.save(Pago.builder()
                .pedido(pedido)
                .metodoPago(metodoPago)
                .monto(total)
                .estado("PENDIENTE")
                .fechaRegistro(LocalDateTime.now())
                .build());

        guardarHistorial(pedido, estadoInicial, null, "Pedido creado");

        //falta registrar cupon y descuentos
        return toResponseDTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerPorId(Integer pedidoId, Integer clienteId) {
        Pedido p = pedidoRepository.findByIdAndClienteIdWithDetails(pedidoId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        return toResponseDTO(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorCliente(Integer clienteId) {
        return pedidoRepository.findByClienteIdWithDetails(clienteId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorEstadosYSede(Integer sedeId, List<String> estados) {
        return pedidoRepository.findBySedeIdAndEstadosWithDetails(sedeId, estados).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public PedidoResponseDTO cambiarEstado(Integer pedidoId, CambiarEstadoPedidoDTO dto, Integer usuarioId) {
        Pedido pedido = pedidoRepository.findByIdWithDetails(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        String actual = pedido.getEstadoPedido().getNombre();
        String nuevo = dto.getNuevoEstado();

        validarTransicion(actual, nuevo);

        EstadoPedido estadoNuevo = estadoPedidoRepository.findByNombre(nuevo)
                .orElseThrow(() -> new BusinessException("Estado no válido: " + nuevo));

        pedido.setEstadoPedido(estadoNuevo);
        pedidoRepository.save(pedido);

        Usuario usuario = usuarioId != null
                ? usuarioRepository.findById(usuarioId).orElse(null)
                : null;
        guardarHistorial(pedido, estadoNuevo, usuario, dto.getObservacion());

        // Stock
        if ("EN_PREPARACION".equals(nuevo) && !"EN_PREPARACION".equals(actual)) {
            stockService.consumirReservas(pedidoId);
        }
        if ("CANCELADO".equals(nuevo)) {
            // solo libera si aún estaba reservado (no consumido)
            stockService.liberarReservas(pedidoId);
        }

        return toResponseDTO(pedido);
    }

    private void validarTransicion(String actual, String nuevo) {
        if (actual.equals(nuevo)) {
            return;
        }
        Map<String, Set<String>> permitidas = Map.of(
                "PENDIENTE", Set.of("CONFIRMADO", "EN_PREPARACION", "CANCELADO"),
                "CONFIRMADO", Set.of("EN_PREPARACION", "CANCELADO"),
                "EN_PREPARACION", Set.of("LISTO", "CANCELADO"),
                "LISTO", Set.of("EN_CAMINO", "CANCELADO"),
                "EN_CAMINO", Set.of("ENTREGADO", "CANCELADO"),
                "ENTREGADO", Set.of(),
                "CANCELADO", Set.of()
        );
        Set<String> next = permitidas.getOrDefault(actual, Set.of());
        if (!next.contains(nuevo)) {
            throw new BusinessException("Transición no permitida: " + actual + " → " + nuevo);
        }
    }

    private void guardarHistorial(Pedido pedido, EstadoPedido estado, Usuario usuario, String obs) {
        historialPedidoRepository.save(HistorialPedido.builder()
                .pedido(pedido)
                .estadoPedido(estado)
                .usuario(usuario)
                .observacion(obs)
                .fechaCambio(LocalDateTime.now())
                .build());
    }

    private PedidoResponseDTO toResponseDTO(Pedido p) {
        List<DetallePedidoResponseDTO> items = p.getDetalles() == null ? List.of()
                : p.getDetalles().stream().map(d -> DetallePedidoResponseDTO.builder()
                .productoId(d.getProducto().getId())
                .nombreProducto(d.getProducto().getNombre())
                .cantidad(d.getCantidad())
                .precioLista(d.getPrecioLista())
                .precioUnitarioFinal(d.getPrecioUnitarioFinal())
                .descuentoUnitario(d.getDescuentoUnitario())
                .subtotal(d.getSubtotal())
                .build()).toList();

        String dirTxt = p.getDireccion() != null ? p.getDireccion().getCalle() : "";

        return PedidoResponseDTO.builder()
                .id(p.getId())
                .estado(p.getEstadoPedido() != null ? p.getEstadoPedido().getNombre() : null)
                .fechaPedido(p.getFechaPedido())
                .sedeId(p.getSede() != null ? p.getSede().getId() : null)
                .sedeNombre(p.getSede() != null ? p.getSede().getNombre() : null)
                .subtotal(p.getSubtotal())
                .descuentoTotal(p.getDescuentoTotal())
                .costoDelivery(p.getCostoDelivery())
                .total(p.getTotal())
                .distanciaKm(p.getDistanciaKm())
                .direccion(dirTxt)
                .observaciones(p.getObservaciones())
                .codigoCupon(p.getCodigoCupon())
                .items(items)
                .build();
    }
}
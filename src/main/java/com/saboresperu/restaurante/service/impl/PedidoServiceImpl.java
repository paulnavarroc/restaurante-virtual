package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.dto.*;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final DireccionRepository direccionRepository;
    private final ProductoRepository productoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PagoRepository pagoRepository;
    private final HistorialPedidoRepository historialPedidoRepository;
    private final UsuarioRepository usuarioRepository;

    private static final BigDecimal COSTO_DELIVERY = new BigDecimal("8.00");
    private static final BigDecimal DELIVERY_GRATIS_DESDE = new BigDecimal("60.00");

    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(Integer clienteId, CrearPedidoRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        Direccion direccion = direccionRepository.findById(dto.getDireccionId())
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

        if (!direccion.getCliente().getId().equals(clienteId)) {
            throw new BusinessException("La dirección no pertenece al cliente");
        }

        EstadoPedido estadoPendiente = estadoPedidoRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new ResourceNotFoundException("Estado PENDIENTE no encontrado"));

        MetodoPago metodoPago = metodoPagoRepository.findById(dto.getMetodoPagoId())
                .orElseThrow(() -> new ResourceNotFoundException("Método de pago no encontrado"));

        // Calcular totales
        BigDecimal subtotal = BigDecimal.ZERO;
        List<DetallePedido> detalles = new ArrayList<>();

        for (DetallePedidoRequestDTO item : dto.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getProductoId()));

            if (!Boolean.TRUE.equals(producto.getDisponible()) || !Boolean.TRUE.equals(producto.getActivo())) {
                throw new RuntimeException("Producto no disponible: " + producto.getNombre());
            }

            BigDecimal precio = producto.getPrecio();
            BigDecimal itemSubtotal = precio.multiply(BigDecimal.valueOf(item.getCantidad()));
            subtotal = subtotal.add(itemSubtotal);

            DetallePedido detalle = DetallePedido.builder()
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(precio)
                    .build();
            detalles.add(detalle);
        }

        BigDecimal costoDelivery = subtotal.compareTo(DELIVERY_GRATIS_DESDE) >= 0
                ? BigDecimal.ZERO
                : COSTO_DELIVERY;

        BigDecimal total = subtotal.add(costoDelivery);

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .direccion(direccion)
                .estadoPedido(estadoPendiente)
                .fechaPedido(LocalDateTime.now())
                .subtotal(subtotal)
                .costoDelivery(costoDelivery)
                .descuento(BigDecimal.ZERO)
                .total(total)
                .observaciones(dto.getObservaciones())
                .build();

        // Relacionar detalles
        for (DetallePedido d : detalles) {
            d.setPedido(pedido);
        }
        pedido.setDetalles(detalles);

        pedido = pedidoRepository.save(pedido);

        // Registrar pago
        Pago pago = Pago.builder()
                .pedido(pedido)
                .metodoPago(metodoPago)
                .monto(total)
                .estado("PENDIENTE")
                .fechaRegistro(LocalDateTime.now())
                .build();
        pagoRepository.save(pago);

        // Historial
        guardarHistorial(pedido, estadoPendiente, null, "Pedido creado");

        return toResponseDTO(pedido);
    }

    /*
    @Override
    public List<PedidoResponseDTO> listarPorEstados(List<String> estados) {
        return pedidoRepository.findByEstados(estados)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
     */

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorEstados(List<String> estados) {
        List<Pedido> pedidos = pedidoRepository.findByEstadosWithDetails(estados);
        return pedidos.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public PedidoResponseDTO cambiarEstado(Integer pedidoId, CambiarEstadoPedidoDTO dto, Integer usuarioId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        EstadoPedido nuevoEstado = estadoPedidoRepository.findByNombre(dto.getNuevoEstado())
                .orElseThrow(() -> new RuntimeException("Estado no válido: " + dto.getNuevoEstado()));

        pedido.setEstadoPedido(nuevoEstado);
        pedidoRepository.save(pedido);

        Usuario usuario = usuarioId != null
                ? usuarioRepository.findById(usuarioId).orElse(null)
                : null;

        guardarHistorial(pedido, nuevoEstado, usuario, dto.getObservacion());

        return toResponseDTO(pedido);
    }

    private void guardarHistorial(Pedido pedido, EstadoPedido estado, Usuario usuario, String observacion) {
        HistorialPedido historial = HistorialPedido.builder()
                .pedido(pedido)
                .estadoPedido(estado)
                .usuario(usuario)
                .observacion(observacion)
                .fechaCambio(LocalDateTime.now())
                .build();
        historialPedidoRepository.save(historial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorCliente(Integer clienteId) {
        return pedidoRepository.findByClienteIdWithDetails(clienteId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerPorId(Integer pedidoId, Integer clienteId) {
        Pedido pedido = pedidoRepository.findByIdAndClienteIdWithDetails(pedidoId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        return toResponseDTO(pedido);
    }


    private PedidoResponseDTO toResponseDTO(Pedido p) {
        List<DetallePedidoResponseDTO> items =
                (p.getDetalles() == null ? List.<DetallePedido>of() : p.getDetalles())
                        .stream()
                        .map(d -> DetallePedidoResponseDTO.builder()
                                .productoId(d.getProducto() != null ? d.getProducto().getId() : null)
                                .nombreProducto(d.getProducto() != null ? d.getProducto().getNombre() : "Producto")
                                .cantidad(d.getCantidad())
                                .precioUnitario(d.getPrecioUnitario())
                                .subtotal(d.getPrecioUnitario()
                                        .multiply(BigDecimal.valueOf(d.getCantidad() != null ? d.getCantidad() : 0)))
                                .build())
                        .toList();

        return PedidoResponseDTO.builder()
                .id(p.getId())
                .estado(p.getEstadoPedido() != null ? p.getEstadoPedido().getNombre() : "")
                .fechaPedido(p.getFechaPedido())
                .subtotal(p.getSubtotal())
                .costoDelivery(p.getCostoDelivery())
                .descuento(p.getDescuento() != null ? p.getDescuento() : BigDecimal.ZERO)
                .total(p.getTotal())
                .observaciones(p.getObservaciones())
                .direccion(p.getDireccion() != null ? p.getDireccion().getDireccionCompleta() : "")
                .items(items)
                .build();
    }

    /*
    private PedidoResponseDTO toResponseDTO(Pedido p) {
        List<DetallePedidoResponseDTO> items = p.getDetalles() == null
                ? List.of()
                : p.getDetalles().stream()
                .map(d -> DetallePedidoResponseDTO.builder()
                        .productoId(d.getProducto() != null ? d.getProducto().getId() : null)
                        .nombreProducto(d.getProducto() != null ? d.getProducto().getNombre() : "Producto")
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getPrecioUnitario()
                                .multiply(BigDecimal.valueOf(d.getCantidad())))
                        .build())
                .toList();

        return PedidoResponseDTO.builder()
                .id(p.getId())  // si tu entidad usa getIdPedido(), pon p.getIdPedido()
                .estado(p.getEstadoPedido() != null ? p.getEstadoPedido().getNombre() : "")
                .fechaPedido(p.getFechaPedido())
                .subtotal(p.getSubtotal())
                .costoDelivery(p.getCostoDelivery())
                .descuento(p.getDescuento() != null ? p.getDescuento() : BigDecimal.ZERO)
                .total(p.getTotal())
                .observaciones(p.getObservaciones())
                .direccion(p.getDireccion() != null ? p.getDireccion().getDireccionCompleta() : "")
                .items(items)
                .build();
    }

     */
}
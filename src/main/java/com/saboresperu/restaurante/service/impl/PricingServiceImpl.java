package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.TiposPromocion;
import com.saboresperu.restaurante.dto.*;
import com.saboresperu.restaurante.entity.Cupon;
import com.saboresperu.restaurante.entity.Promocion;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.repository.CuponRepository;
import com.saboresperu.restaurante.repository.CuponUsoRepository;
import com.saboresperu.restaurante.repository.PromocionProductoRepository;
import com.saboresperu.restaurante.repository.PromocionRepository;
import com.saboresperu.restaurante.service.interfaces.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final PromocionRepository promocionRepository;
    private final PromocionProductoRepository promocionProductoRepository;
    private final CuponRepository cuponRepository;
    private final CuponUsoRepository cuponUsoRepository;

    @Override
    @Transactional(readOnly = true)
    public ResultadoPricingDTO calcular(
            Integer sedeId,
            Integer clienteId,
            Map<Integer, Integer> productoCantidades,
            Map<Integer, BigDecimal> preciosLista,
            Map<Integer, Integer> productoToMenuItemId,
            BigDecimal deliveryBase,
            String codigoCupon
    ) {
        LocalDateTime ahora = LocalDateTime.now();
        List<Promocion> automaticas = promocionRepository.findAutomaticasVigentes(sedeId, ahora);

        List<LineaPrecioDTO> lineas = new ArrayList<>();
        List<DescuentoAplicadoDTO> descuentos = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (Map.Entry<Integer, Integer> e : productoCantidades.entrySet()) {
            Integer productoId = e.getKey();
            int cantidad = e.getValue();
            BigDecimal precioLista = preciosLista.get(productoId)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal precioFinal = precioLista;
            BigDecimal descUnit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            Integer promoId = null;
            String promoNombre = null;

            // Mejor descuento automático de producto (no se apilan entre sí en v1)
            BigDecimal mejorFinal = precioLista;
            Promocion mejorPromo = null;

            for (Promocion promo : automaticas) {
                if (!esPromoProducto(promo.getTipo())) continue;
                if (!aplicaAProducto(promo, productoId)) continue;

                BigDecimal candidato = aplicarPromoUnitaria(precioLista, promo);
                if (candidato.compareTo(mejorFinal) < 0) {
                    mejorFinal = candidato;
                    mejorPromo = promo;
                }
            }

            if (mejorPromo != null) {
                precioFinal = mejorFinal;
                descUnit = precioLista.subtract(precioFinal).setScale(2, RoundingMode.HALF_UP);
                promoId = mejorPromo.getId();
                promoNombre = mejorPromo.getNombre();
                descuentos.add(DescuentoAplicadoDTO.builder()
                        .tipo("PRODUCTO")
                        .promocionId(promoId)
                        .monto(descUnit.multiply(BigDecimal.valueOf(cantidad))
                                .setScale(2, RoundingMode.HALF_UP))
                        .descripcion(promoNombre)
                        .build());
            }

            BigDecimal lineaSub = precioFinal.multiply(BigDecimal.valueOf(cantidad))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineaSub);

            lineas.add(LineaPrecioDTO.builder()
                    .productoId(productoId)
                    .menuDiaItemId(productoToMenuItemId != null
                            ? productoToMenuItemId.get(productoId) : null)
                    .cantidad(cantidad)
                    .precioLista(precioLista)
                    .precioUnitarioFinal(precioFinal)
                    .descuentoUnitario(descUnit)
                    .subtotal(lineaSub)
                    .promocionId(promoId)
                    .promocionNombre(promoNombre)
                    .build());
        }

        // Cupón (v1: % o monto sobre subtotal ya con promos de ítem)
        BigDecimal descuentoCupon = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        String codigoAplicado = null;
        Integer cuponId = null;
        Integer promoCuponId = null;

        if (codigoCupon != null && !codigoCupon.isBlank()) {
            Cupon cupon = cuponRepository.findActivoByCodigo(codigoCupon.trim())
                    .orElseThrow(() -> new BusinessException("Cupón inválido"));

            Promocion promo = cupon.getPromocion();
            validarCupon(cupon, promo, sedeId, clienteId, ahora);

            if (TiposPromocion.PORCENTAJE_PRODUCTO.equals(promo.getTipo())
                    || "PORCENTAJE_PEDIDO".equals(promo.getTipo())
                    || TiposPromocion.CUPON.equals(promo.getTipo())) {
                // Si valor es porcentaje (convención: tipo CUPON + valor = %)
                if (promo.getValor() != null && promo.getValor().compareTo(new BigDecimal("100")) <= 0
                        && (TiposPromocion.PORCENTAJE_PRODUCTO.equals(promo.getTipo())
                        || TiposPromocion.CUPON.equals(promo.getTipo()))) {
                    descuentoCupon = subtotal.multiply(promo.getValor())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                }
            }
            if (TiposPromocion.MONTO_PRODUCTO.equals(promo.getTipo())
                    || (promo.getValor() != null && TiposPromocion.CUPON.equals(promo.getTipo())
                    && promo.getValor().compareTo(new BigDecimal("100")) > 0)) {
                // monto fijo
                descuentoCupon = promo.getValor().min(subtotal).setScale(2, RoundingMode.HALF_UP);
            }
            // Convención simple v1 para CUPON:
            // valor <= 100 → porcentaje; valor > 100 → no usar; mejor: campo separado.
            // Alternativa clara:
            descuentoCupon = calcularDescuentoCupon(subtotal, promo);

            if (descuentoCupon.signum() > 0) {
                codigoAplicado = cupon.getCodigo();
                cuponId = cupon.getId();
                promoCuponId = promo.getId();
                descuentos.add(DescuentoAplicadoDTO.builder()
                        .tipo("CUPON")
                        .promocionId(promoCuponId)
                        .cuponId(cuponId)
                        .monto(descuentoCupon)
                        .descripcion("Cupón " + codigoAplicado)
                        .build());
            }
        }

        BigDecimal delivery = deliveryBase != null
                ? deliveryBase.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        // Delivery gratis automático
        for (Promocion promo : automaticas) {
            if (TiposPromocion.DELIVERY_GRATIS.equals(promo.getTipo())) {
                // opcional: valor = monto mínimo de subtotal
                if (promo.getValor() == null || subtotal.compareTo(promo.getValor()) >= 0) {
                    if (delivery.signum() > 0) {
                        descuentos.add(DescuentoAplicadoDTO.builder()
                                .tipo("DELIVERY")
                                .promocionId(promo.getId())
                                .monto(delivery)
                                .descripcion(promo.getNombre())
                                .build());
                    }
                    delivery = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                    break;
                }
            }
        }

        BigDecimal descuentoTotal = descuentos.stream()
                .map(DescuentoAplicadoDTO::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // total = subtotal líneas - solo descuento cupón + delivery
        // (descuentos de producto ya están en el subtotal de líneas)
        BigDecimal descuentoSoloCupon = descuentoCupon;
        BigDecimal total = subtotal.subtract(descuentoSoloCupon).add(delivery)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        return ResultadoPricingDTO.builder()
                .lineas(lineas)
                .subtotal(subtotal)
                .descuentoTotal(descuentoSoloCupon.add(
                        descuentos.stream()
                                .filter(d -> "PRODUCTO".equals(d.getTipo()))
                                .map(DescuentoAplicadoDTO::getMonto)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .costoDelivery(delivery)
                .total(total)
                .descuentos(descuentos)
                .codigoCuponAplicado(codigoAplicado)
                .build();
    }

    private boolean esPromoProducto(String tipo) {
        return TiposPromocion.PORCENTAJE_PRODUCTO.equals(tipo)
                || TiposPromocion.MONTO_PRODUCTO.equals(tipo)
                || TiposPromocion.HAPPY_HOUR.equals(tipo);
    }

    private boolean aplicaAProducto(Promocion promo, Integer productoId) {
        List<?> links = promocionProductoRepository.findByIdPromocion(promo.getId());
        if (links.isEmpty()) {
            return true; // sin restricción = todos
        }
        return promocionProductoRepository.existsByIdPromocionAndIdProducto(promo.getId(), productoId);
    }

    private BigDecimal aplicarPromoUnitaria(BigDecimal precioLista, Promocion promo) {
        BigDecimal valor = promo.getValor() != null ? promo.getValor() : BigDecimal.ZERO;
        if (TiposPromocion.PORCENTAJE_PRODUCTO.equals(promo.getTipo())
                || TiposPromocion.HAPPY_HOUR.equals(promo.getTipo())) {
            BigDecimal desc = precioLista.multiply(valor)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            return precioLista.subtract(desc).max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        if (TiposPromocion.MONTO_PRODUCTO.equals(promo.getTipo())) {
            return precioLista.subtract(valor).max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return precioLista;
    }

    private BigDecimal calcularDescuentoCupon(BigDecimal subtotal, Promocion promo) {
        if (promo.getValor() == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        // Convención v1: si tipo es PORCENTAJE_PRODUCTO o CUPON y valor <= 100 → %
        if (TiposPromocion.PORCENTAJE_PRODUCTO.equals(promo.getTipo())
                || (TiposPromocion.CUPON.equals(promo.getTipo())
                && promo.getValor().compareTo(new BigDecimal("100")) <= 0)) {
            return subtotal.multiply(promo.getValor())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        // monto fijo
        return promo.getValor().min(subtotal).setScale(2, RoundingMode.HALF_UP);
    }

    private void validarCupon(Cupon cupon, Promocion promo, Integer sedeId,
                              Integer clienteId, LocalDateTime ahora) {
        if (ahora.isBefore(promo.getFechaHoraInicio()) || ahora.isAfter(promo.getFechaHoraFin())) {
            throw new BusinessException("Cupón fuera de vigencia");
        }
        if (promo.getSede() != null && !promo.getSede().getId().equals(sedeId)) {
            throw new BusinessException("Cupón no válido en esta sede");
        }
        if (cupon.getUsoMaxGlobal() != null
                && cupon.getUsosActuales() >= cupon.getUsoMaxGlobal()) {
            throw new BusinessException("Cupón agotado");
        }
        if (promo.getCupoGlobal() != null
                && promo.getUsosActuales() >= promo.getCupoGlobal()) {
            throw new BusinessException("Promoción agotada");
        }
        if (clienteId != null && cupon.getUsoMaxPorCliente() != null) {
            long usos = cuponUsoRepository.countByCuponIdAndClienteId(cupon.getId(), clienteId);
            if (usos >= cupon.getUsoMaxPorCliente()) {
                throw new BusinessException("Ya usaste este cupón el máximo permitido");
            }
        }
    }
}
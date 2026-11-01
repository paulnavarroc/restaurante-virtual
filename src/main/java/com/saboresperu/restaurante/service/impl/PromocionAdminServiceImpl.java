package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.TiposPromocion;
import com.saboresperu.restaurante.dto.*;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.PromocionAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PromocionAdminServiceImpl implements PromocionAdminService {

    private final PromocionRepository promocionRepository;
    private final PromocionProductoRepository promocionProductoRepository;
    private final PromocionAuditoriaRepository promocionAuditoriaRepository;
    private final CuponRepository cuponRepository;
    private final SedeRepository sedeRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PromocionListDTO> listar() {
        return promocionRepository.findAllByOrderByFechaHoraInicioDesc().stream()
                .map(p -> PromocionListDTO.builder()
                        .id(p.getId())
                        .nombre(p.getNombre())
                        .tipo(p.getTipo())
                        .valor(p.getValor())
                        .sedeNombre(p.getSede() != null ? p.getSede().getNombre() : "Todas")
                        .fechaHoraInicio(p.getFechaHoraInicio())
                        .fechaHoraFin(p.getFechaHoraFin())
                        .activa(p.getActiva())
                        .requiereCodigo(p.getRequiereCodigo())
                        .usosActuales(p.getUsosActuales())
                        .cupoGlobal(p.getCupoGlobal())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionFormDTO obtenerForm(Integer id) {
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada"));
        List<Integer> productoIds = promocionProductoRepository.findByIdPromocion(id).stream()
                .map(PromocionProducto::getIdProducto)
                .toList();
        return PromocionFormDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .tipo(p.getTipo())
                .valor(p.getValor())
                .llevaN(p.getLlevaN())
                .pagaM(p.getPagaM())
                .sedeId(p.getSede() != null ? p.getSede().getId() : null)
                .fechaHoraInicio(p.getFechaHoraInicio())
                .fechaHoraFin(p.getFechaHoraFin())
                .cupoGlobal(p.getCupoGlobal())
                .combinable(p.getCombinable())
                .requiereCodigo(p.getRequiereCodigo())
                .prioridad(p.getPrioridad())
                .activa(p.getActiva())
                .productoIds(productoIds)
                .build();
    }

    @Override
    @Transactional
    public Integer crear(PromocionFormDTO form, Integer usuarioId) {
        validarForm(form);
        Promocion p = new Promocion();
        p.setUsosActuales(0);
        aplicar(p, form);
        p = promocionRepository.save(p);
        sincronizarProductos(p.getId(), form.getProductoIds());
        auditar(p.getId(), usuarioId, "CREACION", null, p.getNombre());
        return p.getId();
    }

    @Override
    @Transactional
    public void actualizar(Integer id, PromocionFormDTO form, Integer usuarioId) {
        validarForm(form);
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada"));
        String antes = p.getNombre() + "/" + p.getValor();
        aplicar(p, form);
        promocionRepository.save(p);
        sincronizarProductos(id, form.getProductoIds());
        auditar(id, usuarioId, "ACTUALIZACION", antes, form.getNombre() + "/" + form.getValor());
    }

    @Override
    @Transactional
    public void activar(Integer id, boolean activa, Integer usuarioId) {
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada"));
        p.setActiva(activa);
        promocionRepository.save(p);
        auditar(id, usuarioId, "ACTIVA", String.valueOf(!activa), String.valueOf(activa));
    }

    private void aplicar(Promocion p, PromocionFormDTO form) {
        p.setNombre(form.getNombre().trim());
        p.setTipo(form.getTipo());
        p.setValor(form.getValor());
        p.setLlevaN(form.getLlevaN());
        p.setPagaM(form.getPagaM());
        p.setSede(form.getSedeId() != null
                ? sedeRepository.findById(form.getSedeId()).orElseThrow()
                : null);
        p.setFechaHoraInicio(form.getFechaHoraInicio());
        p.setFechaHoraFin(form.getFechaHoraFin());
        p.setCupoGlobal(form.getCupoGlobal());
        p.setCombinable(form.getCombinable() == null || form.getCombinable());
        p.setRequiereCodigo(Boolean.TRUE.equals(form.getRequiereCodigo()));
        p.setPrioridad(form.getPrioridad() != null ? form.getPrioridad() : 100);
        p.setActiva(form.getActiva() == null || form.getActiva());
    }

    private void validarForm(PromocionFormDTO form) {
        if (form.getFechaHoraFin() == null || form.getFechaHoraInicio() == null
                || !form.getFechaHoraFin().isAfter(form.getFechaHoraInicio())) {
            throw new BusinessException("El fin debe ser posterior al inicio");
        }
        Set<String> tipos = Set.of(
                TiposPromocion.PORCENTAJE_PRODUCTO,
                TiposPromocion.MONTO_PRODUCTO,
                TiposPromocion.NX_M,
                TiposPromocion.CUPON,
                TiposPromocion.HAPPY_HOUR,
                TiposPromocion.DELIVERY_GRATIS
        );
        if (!tipos.contains(form.getTipo())) {
            throw new BusinessException("Tipo de promoción no válido");
        }
        if (TiposPromocion.NX_M.equals(form.getTipo())
                && (form.getLlevaN() == null || form.getPagaM() == null
                || form.getLlevaN() <= form.getPagaM())) {
            throw new BusinessException("NxM inválido (lleva N > paga M)");
        }
    }

    private void sincronizarProductos(Integer promocionId, List<Integer> productoIds) {
        promocionProductoRepository.deleteByIdPromocion(promocionId);
        if (productoIds == null || productoIds.isEmpty()) {
            return;
        }
        Set<Integer> uniques = new HashSet<>(productoIds);
        for (Integer pid : uniques) {
            if (!productoRepository.existsById(pid)) {
                throw new ResourceNotFoundException("Producto no encontrado: " + pid);
            }
            promocionProductoRepository.save(PromocionProducto.builder()
                    .idPromocion(promocionId)
                    .idProducto(pid)
                    .build());
        }
    }

    private void auditar(Integer promocionId, Integer usuarioId, String campo,
                         String anterior, String nuevo) {
        if (usuarioId == null) return;
        Usuario u = usuarioRepository.findById(usuarioId).orElse(null);
        if (u == null) return;
        promocionAuditoriaRepository.save(PromocionAuditoria.builder()
                .promocion(promocionRepository.getReferenceById(promocionId))
                .usuario(u)
                .campo(campo)
                .valorAnterior(anterior)
                .valorNuevo(nuevo)
                .fecha(LocalDateTime.now())
                .build());
    }

    // ----- Cupones -----

    @Override
    @Transactional(readOnly = true)
    public List<CuponListDTO> listarCupones() {
        return cuponRepository.findAllWithPromocion().stream()
                .map(c -> CuponListDTO.builder()
                        .id(c.getId())
                        .codigo(c.getCodigo())
                        .promocionNombre(c.getPromocion().getNombre())
                        .usoMaxGlobal(c.getUsoMaxGlobal())
                        .usoMaxPorCliente(c.getUsoMaxPorCliente())
                        .usosActuales(c.getUsosActuales())
                        .activo(c.getActivo())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public Integer crearCupon(CuponFormDTO form) {
        String codigo = form.getCodigo().trim().toUpperCase();
        if (cuponRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new BusinessException("El código ya existe");
        }
        Promocion promo = promocionRepository.findById(form.getPromocionId())
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada"));
        if (!Boolean.TRUE.equals(promo.getRequiereCodigo())
                && !TiposPromocion.CUPON.equals(promo.getTipo())) {
            // permitir asociar cupón a promo marcada requiereCodigo o tipo CUPON
        }
        Cupon c = Cupon.builder()
                .promocion(promo)
                .codigo(codigo)
                .usoMaxGlobal(form.getUsoMaxGlobal())
                .usoMaxPorCliente(form.getUsoMaxPorCliente())
                .usosActuales(0)
                .activo(form.getActivo() == null || form.getActivo())
                .build();
        return cuponRepository.save(c).getId();
    }

    @Override
    @Transactional
    public void activarCupon(Integer id, boolean activo) {
        Cupon c = cuponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado"));
        c.setActivo(activo);
        cuponRepository.save(c);
    }
}
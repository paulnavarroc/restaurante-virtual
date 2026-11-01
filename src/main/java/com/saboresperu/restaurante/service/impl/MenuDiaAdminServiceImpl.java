package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.EstadosMenuDia;
import com.saboresperu.restaurante.dto.*;
import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.*;
import com.saboresperu.restaurante.service.interfaces.MenuDiaAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuDiaAdminServiceImpl implements MenuDiaAdminService {

    private final MenuDiaRepository menuDiaRepository;
    private final MenuDiaItemRepository menuDiaItemRepository;
    private final SedeRepository sedeRepository;
    private final SedeTurnoRepository sedeTurnoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MenuDiaAdminDTO> listarPorSede(Integer sedeId) {
        return menuDiaRepository.findBySedeIdOrderByFechaDesc(sedeId).stream()
                .map(m -> MenuDiaAdminDTO.builder()
                        .id(m.getId())
                        .sedeId(m.getSede().getId())
                        .sedeNombre(m.getSede().getNombre())
                        .fecha(m.getFecha())
                        .estado(m.getEstado())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuDiaAdminDTO obtener(Integer menuDiaId) {
        MenuDia m = menuDiaRepository.findByIdWithDetails(menuDiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Menú no encontrado"));
        return toAdminDTO(m);
    }

    @Override
    @Transactional
    public MenuDiaAdminDTO crearBorrador(CrearMenuDiaRequestDTO request) {
        Sede sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        menuDiaRepository.findBySedeIdAndFecha(sede.getId(), request.getFecha())
                .ifPresent(m -> {
                    throw new BusinessException("Ya existe menú para esa sede y fecha");
                });

        MenuDia menu = MenuDia.builder()
                .sede(sede)
                .fecha(request.getFecha())
                .estado(EstadosMenuDia.BORRADOR)
                .build();
        menu = menuDiaRepository.save(menu);
        return obtener(menu.getId());
    }

    @Override
    @Transactional
    public MenuDiaAdminDTO agregarItem(Integer menuDiaId, CrearMenuDiaRequestDTO request) {
        MenuDia menu = menuDiaRepository.findByIdWithDetails(menuDiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Menú no encontrado"));

        if (EstadosMenuDia.CERRADO.equals(menu.getEstado())) {
            throw new BusinessException("No se puede modificar un menú cerrado");
        }

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        boolean exists = menu.getItems().stream()
                .anyMatch(i -> i.getProducto().getId().equals(producto.getId()));
        if (exists) {
            throw new BusinessException("El producto ya está en el menú");
        }

        Set<SedeTurno> turnos = resolverTurnos(menu.getSede().getId(), request.getSedeTurnoIds());

        MenuDiaItem item = MenuDiaItem.builder()
                .menuDia(menu)
                .producto(producto)
                .disponible(true)
                .precioDia(request.getPrecioDia())
                .cupoMaximo(request.getCupoMaximo())
                .cantidadVendida(0)
                .turnos(turnos)
                .build();

        menu.getItems().add(item);
        menuDiaRepository.save(menu);
        return obtener(menuDiaId);
    }

    private Set<SedeTurno> resolverTurnos(Integer sedeId, List<Integer> ids) {
        List<SedeTurno> activos = sedeTurnoRepository.findBySedeIdAndActivoTrue(sedeId);
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>(activos);
        }
        Set<Integer> wanted = new HashSet<>(ids);
        Set<SedeTurno> selected = activos.stream()
                .filter(t -> wanted.contains(t.getId()))
                .collect(Collectors.toSet());
        if (selected.isEmpty()) {
            throw new BusinessException("Debes elegir al menos un turno válido");
        }
        return selected;
    }

    @Override
    @Transactional
    public void quitarItem(Integer menuDiaId, Integer menuDiaItemId) {
        MenuDia menu = menuDiaRepository.findByIdWithDetails(menuDiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Menú no encontrado"));
        if (EstadosMenuDia.CERRADO.equals(menu.getEstado())) {
            throw new BusinessException("Menú cerrado");
        }
        menu.getItems().removeIf(i -> i.getId().equals(menuDiaItemId));
        menuDiaRepository.save(menu);
        // orphanRemoval elimina el item
    }

    @Override
    @Transactional
    public void marcarDisponible(Integer menuDiaItemId, boolean disponible) {
        MenuDiaItem item = menuDiaItemRepository.findById(menuDiaItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado"));
        item.setDisponible(disponible);
        menuDiaItemRepository.save(item);
    }

    @Override
    @Transactional
    public MenuDiaAdminDTO publicar(Integer menuDiaId, Integer usuarioId) {
        MenuDia menu = menuDiaRepository.findByIdWithDetails(menuDiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Menú no encontrado"));

        if (menu.getItems() == null || menu.getItems().isEmpty()) {
            throw new BusinessException("No puedes publicar un menú sin platos");
        }
        if (EstadosMenuDia.CERRADO.equals(menu.getEstado())) {
            throw new BusinessException("El menú está cerrado");
        }

        menu.setEstado(EstadosMenuDia.PUBLICADO);
        menu.setFechaPublicacion(LocalDateTime.now());
        if (usuarioId != null) {
            usuarioRepository.findById(usuarioId).ifPresent(menu::setUsuarioPublicacion);
        }
        menuDiaRepository.save(menu);
        return obtener(menuDiaId);
    }

    @Override
    @Transactional
    public MenuDiaAdminDTO cerrar(Integer menuDiaId) {
        MenuDia menu = menuDiaRepository.findById(menuDiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Menú no encontrado"));
        menu.setEstado(EstadosMenuDia.CERRADO);
        menuDiaRepository.save(menu);
        return obtener(menuDiaId);
    }

    private MenuDiaAdminDTO toAdminDTO(MenuDia m) {
        List<MenuDiaItemAdminDTO> items = m.getItems() == null ? List.of()
                : m.getItems().stream().map(i -> MenuDiaItemAdminDTO.builder()
                .id(i.getId())
                .productoId(i.getProducto().getId())
                .productoNombre(i.getProducto().getNombre())
                .disponible(i.getDisponible())
                .precioDia(i.getPrecioDia() != null ? i.getPrecioDia() : i.getProducto().getPrecioBase())
                .cupoMaximo(i.getCupoMaximo())
                .cantidadVendida(i.getCantidadVendida())
                .sedeTurnoIds(i.getTurnos().stream().map(SedeTurno::getId).toList())
                .build()).toList();

        return MenuDiaAdminDTO.builder()
                .id(m.getId())
                .sedeId(m.getSede().getId())
                .sedeNombre(m.getSede().getNombre())
                .fecha(m.getFecha())
                .estado(m.getEstado())
                .items(items)
                .build();
    }
}
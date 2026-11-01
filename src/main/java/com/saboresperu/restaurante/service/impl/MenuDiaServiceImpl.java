package com.saboresperu.restaurante.service.impl;

import com.saboresperu.restaurante.domain.EstadosMenuDia;
import com.saboresperu.restaurante.dto.MenuDiaDTO;
import com.saboresperu.restaurante.dto.MenuDiaItemDTO;
import com.saboresperu.restaurante.entity.MenuDia;
import com.saboresperu.restaurante.entity.MenuDiaItem;
import com.saboresperu.restaurante.exception.BusinessException;
import com.saboresperu.restaurante.exception.ResourceNotFoundException;
import com.saboresperu.restaurante.repository.MenuDiaItemRepository;
import com.saboresperu.restaurante.repository.MenuDiaRepository;
import com.saboresperu.restaurante.service.interfaces.MenuDiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuDiaServiceImpl implements MenuDiaService {

    private final MenuDiaRepository menuDiaRepository;
    private final MenuDiaItemRepository menuDiaItemRepository;

    @Override
    @Transactional(readOnly = true)
    public MenuDiaDTO obtenerPublicado(Integer sedeId, LocalDate fecha) {
        MenuDia menu = menuDiaRepository.findPublicadoWithDetails(sedeId, fecha, EstadosMenuDia.PUBLICADO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay menú publicado para esa sede y fecha"));

        return MenuDiaDTO.builder()
                .id(menu.getId())
                .sedeId(menu.getSede().getId())
                .sedeNombre(menu.getSede().getNombre())
                .fecha(menu.getFecha())
                .estado(menu.getEstado())
                .items(menu.getItems().stream().map(this::toItemDTO).toList())
                .build();
    }

    @Override
    @Transactional
    public void marcarItemAgotado(Integer menuDiaItemId, boolean disponible) {
        MenuDiaItem item = menuDiaItemRepository.findById(menuDiaItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem de menú no encontrado"));
        item.setDisponible(disponible);
        menuDiaItemRepository.save(item);
    }

    private MenuDiaItemDTO toItemDTO(MenuDiaItem item) {
        BigDecimal precioLista = item.getPrecioDia() != null
                ? item.getPrecioDia()
                : item.getProducto().getPrecioBase();

        boolean agotado = !Boolean.TRUE.equals(item.getDisponible());
        // cupo del día
        if (item.getCupoMaximo() != null
                && item.getCantidadVendida() != null
                && item.getCantidadVendida() >= item.getCupoMaximo()) {
            agotado = true;
        }

        return MenuDiaItemDTO.builder()
                .id(item.getId())
                .productoId(item.getProducto().getId())
                .nombre(item.getProducto().getNombre())
                .descripcion(item.getProducto().getDescripcion())
                .imagenUrl(item.getProducto().getImagenUrl())
                .precioLista(precioLista)
                .precioFinal(precioLista) // promos en siguiente iteración
                .disponible(!agotado)
                .agotado(agotado)
                .categoria(item.getProducto().getCategoria() != null
                        ? item.getProducto().getCategoria().getNombre() : null)
                .region(item.getProducto().getRegion() != null
                        ? item.getProducto().getRegion().getNombre() : null)
                .turnos(item.getTurnos().stream()
                        .map(t -> t.getTurno())
                        .collect(Collectors.toList()))
                .build();
    }
}
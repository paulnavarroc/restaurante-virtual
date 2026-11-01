package com.saboresperu.restaurante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoDTO {

    public static final String SESSION_KEY = "CARRITO_V2";

    @Builder.Default
    private List<CarritoItemDTO> items = new ArrayList<>();

    private Integer sedeId; // fijada al primer ítem

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CarritoItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public int getTotalUnidades() {
        return items.stream().mapToInt(i -> i.getCantidad() != null ? i.getCantidad() : 0).sum();
    }

    public void agregar(CarritoItemDTO nuevo) {
        if (sedeId == null) {
            sedeId = nuevo.getSedeId();
        } else if (nuevo.getSedeId() != null && !sedeId.equals(nuevo.getSedeId())) {
            throw new IllegalStateException(
                    "El carrito solo admite productos de una misma cocina/sede");
        }

        Optional<CarritoItemDTO> existing = items.stream()
                .filter(i -> sameLine(i, nuevo))
                .findFirst();

        if (existing.isPresent()) {
            CarritoItemDTO e = existing.get();
            e.setCantidad(e.getCantidad() + nuevo.getCantidad());
        } else {
            items.add(nuevo);
        }
    }

    public void actualizarCantidad(Integer productoId, Integer menuDiaItemId, int cantidad) {
        items.stream()
                .filter(i -> match(i, productoId, menuDiaItemId))
                .findFirst()
                .ifPresent(i -> {
                    if (cantidad <= 0) {
                        items.remove(i);
                    } else {
                        i.setCantidad(cantidad);
                    }
                });
        if (items.isEmpty()) {
            sedeId = null;
        }
    }

    public void eliminar(Integer productoId, Integer menuDiaItemId) {
        items.removeIf(i -> match(i, productoId, menuDiaItemId));
        if (items.isEmpty()) {
            sedeId = null;
        }
    }

    public void limpiar() {
        items.clear();
        sedeId = null;
    }

    private boolean sameLine(CarritoItemDTO a, CarritoItemDTO b) {
        return match(a, b.getProductoId(), b.getMenuDiaItemId());
    }

    private boolean match(CarritoItemDTO i, Integer productoId, Integer menuDiaItemId) {
        if (menuDiaItemId != null && i.getMenuDiaItemId() != null) {
            return menuDiaItemId.equals(i.getMenuDiaItemId());
        }
        return productoId != null && productoId.equals(i.getProductoId());
    }
}
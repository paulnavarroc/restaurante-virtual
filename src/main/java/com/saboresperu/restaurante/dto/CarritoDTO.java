package com.saboresperu.restaurante.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CarritoDTO {
    private List<CarritoItemDTO> items = new ArrayList<>();

    public void agregarItem(CarritoItemDTO item) {
        for (CarritoItemDTO existente : items) {
            if (existente.getProductoId().equals(item.getProductoId())) {
                existente.setCantidad(existente.getCantidad() + item.getCantidad());
                return;
            }
        }
        items.add(item);
    }

    public void actualizarCantidad(Integer productoId, int cantidad) {
        if (cantidad <= 0) {
            eliminarItem(productoId);
            return;
        }
        items.stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst()
                .ifPresent(i -> i.setCantidad(cantidad));
    }

    public void eliminarItem(Integer productoId) {
        items.removeIf(i -> i.getProductoId().equals(productoId));
    }

    public void limpiar() {
        items.clear();
    }

    public int getTotalItems() {
        return items.stream().mapToInt(CarritoItemDTO::getCantidad).sum();
    }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CarritoItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getCostoDelivery() {
        // Envío gratis desde S/ 60
        return getSubtotal().compareTo(new BigDecimal("60")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("8.00");
    }

    public BigDecimal getTotal() {
        return getSubtotal().add(getCostoDelivery());
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
package com.saboresperu.restaurante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDiaItemAdminDTO {
    private Integer id;
    private Integer productoId;
    private String productoNombre;
    private Boolean disponible;
    private BigDecimal precioDia;
    private Integer cupoMaximo;
    private Integer cantidadVendida;
    private List<Integer> sedeTurnoIds;
}
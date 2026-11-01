package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgregarMenuItemRequestDTO {
    @NotNull
    private Integer productoId;
    private BigDecimal precioDia;
    private Integer cupoMaximo;
    /** Ids de SedeTurno; vacío = todos los turnos activos de la sede */
    private List<Integer> sedeTurnoIds = new ArrayList<>();
}

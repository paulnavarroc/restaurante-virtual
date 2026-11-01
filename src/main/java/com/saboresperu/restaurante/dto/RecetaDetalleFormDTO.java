package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecetaDetalleFormDTO {
    @NotNull
    private Integer insumoId;
    @NotNull @DecimalMin("0.0001")
    private BigDecimal cantidad;
}
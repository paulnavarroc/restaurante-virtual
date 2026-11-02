package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntradaStockFormDTO {
    @NotNull
    private Integer sedeId;
    @NotNull
    private Integer insumoId;
    @NotNull
    @DecimalMin("0.0001")
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private String observacion;
    private String lote;
    private LocalDate fechaVencimiento;
    private String observaciones;
}

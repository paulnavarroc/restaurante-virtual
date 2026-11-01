package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsumoFormDTO {
    private Integer id;
    @NotBlank
    private String nombre;
    @NotBlank
    private String unidad; // kg, g, L, ml, und
    private Boolean stockIlimitado;
    private BigDecimal stockMinimoAlerta;
    private Boolean activo;
}
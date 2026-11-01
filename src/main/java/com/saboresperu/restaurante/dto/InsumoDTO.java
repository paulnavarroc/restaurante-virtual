package com.saboresperu.restaurante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsumoDTO {
    private Integer id;
    private String nombre;
    private String unidad;
    private Boolean stockIlimitado;
    private BigDecimal stockMinimoAlerta;
    private Boolean activo;
}
package com.saboresperu.restaurante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromocionListDTO {
    private Integer id;
    private String nombre;
    private String tipo;
    private BigDecimal valor;
    private String sedeNombre;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Boolean activa;
    private Boolean requiereCodigo;
    private Integer usosActuales;
    private Integer cupoGlobal;
}

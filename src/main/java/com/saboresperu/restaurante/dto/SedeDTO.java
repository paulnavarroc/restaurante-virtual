package com.saboresperu.restaurante.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SedeDTO {
    private Integer id;
    private String nombre;
    private String direccion;
    private BigDecimal lat;
    private BigDecimal lng;
    private BigDecimal radioCoberturaKm;
    private Boolean activa;
}
package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DireccionFormDTO {

    private Integer id;

    private String alias;

    @NotBlank
    private String idDepartamento; // "15"

    @NotBlank
    private String idProvincia;    // "1501"

    @NotBlank
    private String idDistrito;     // "150122"

    private String urbanizacion;

    @NotBlank
    private String calle;

    private String numero;

    private String referencia;

    private BigDecimal lat;
    private BigDecimal lng;

    private Boolean esPrincipal;
}
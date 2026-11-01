package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoFormDTO {

    private Integer id;

    @NotBlank
    private String nombre;

    private String descripcion;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal precioBase;

    private String imagenUrl;

    private Integer tiempoPreparacionMin;

    @NotNull
    private Integer categoriaId;

    private Integer regionId;

    private Boolean stockIlimitado;

    private Boolean activo;
}
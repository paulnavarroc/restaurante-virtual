package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoRequestDTO {

    @NotBlank
    private String nombre;

    private String descripcion;

    @NotNull @DecimalMin("0.0")
    private BigDecimal precio;

    private String imagenUrl;
    private Integer tiempoPreparacion;
    private Boolean disponible = true;

    @NotNull
    private Integer regionId;

    @NotNull
    private Integer categoriaId;
}
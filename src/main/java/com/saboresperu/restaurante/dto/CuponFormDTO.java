package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuponFormDTO {
    private Integer id;
    @NotNull
    private Integer promocionId;
    @NotBlank
    private String codigo;
    private Integer usoMaxGlobal;
    private Integer usoMaxPorCliente;
    private Boolean activo;
}
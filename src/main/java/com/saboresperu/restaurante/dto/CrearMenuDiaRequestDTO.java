package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearMenuDiaRequestDTO {
    @NotNull
    private Integer sedeId;
    @NotNull
    private LocalDate fecha;
}
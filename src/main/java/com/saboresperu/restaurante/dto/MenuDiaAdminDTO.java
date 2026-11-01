package com.saboresperu.restaurante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDiaAdminDTO {
    private Integer id;
    private Integer sedeId;
    private String sedeNombre;
    private LocalDate fecha;
    private String estado;
    private List<MenuDiaItemAdminDTO> items;
}
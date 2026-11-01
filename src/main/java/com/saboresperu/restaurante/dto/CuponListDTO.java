package com.saboresperu.restaurante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuponListDTO {
    private Integer id;
    private String codigo;
    private String promocionNombre;
    private Integer usoMaxGlobal;
    private Integer usoMaxPorCliente;
    private Integer usosActuales;
    private Boolean activo;
}

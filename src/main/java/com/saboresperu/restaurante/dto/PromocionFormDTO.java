package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromocionFormDTO {
    private Integer id;

    @NotBlank
    private String nombre;

    @NotBlank
    private String tipo; // PORCENTAJE_PRODUCTO, MONTO_PRODUCTO, CUPON, DELIVERY_GRATIS, HAPPY_HOUR

    private BigDecimal valor;
    private Integer llevaN;
    private Integer pagaM;
    private Integer sedeId; // null = todas

    @NotNull
    private LocalDateTime fechaHoraInicio;
    @NotNull
    private LocalDateTime fechaHoraFin;

    private Integer cupoGlobal;
    private Boolean combinable;
    private Boolean requiereCodigo;
    private Integer prioridad;
    private Boolean activa;

    /** Productos alcanzados; vacío = todos (en promos de producto) */
    @Builder.Default
    private List<Integer> productoIds = new ArrayList<>();
}
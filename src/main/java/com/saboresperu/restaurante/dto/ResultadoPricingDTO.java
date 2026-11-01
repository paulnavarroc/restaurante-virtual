package com.saboresperu.restaurante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoPricingDTO {
    @Builder.Default
    private List<LineaPrecioDTO> lineas = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal descuentoTotal;
    private BigDecimal costoDelivery;
    private BigDecimal total;
    @Builder.Default
    private List<DescuentoAplicadoDTO> descuentos = new ArrayList<>();
    private String codigoCuponAplicado;
}
package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddressFormDto(
        Integer id,

        @Size(max = 50)
        String alias,

        @NotBlank(message = "Selecciona el distrito")
        @Size(min = 6, max = 6, message = "Código de distrito inválido")
        String districtId,

        @Size(max = 120)
        String urbanization,

        @NotBlank(message = "La calle es obligatoria")
        @Size(max = 200)
        String street,

        @Size(max = 30)
        String number,

        @Size(max = 300)
        String reference

        /** opcional */
        //java.math.BigDecimal lat,

        //java.math.BigDecimal lng
) {}
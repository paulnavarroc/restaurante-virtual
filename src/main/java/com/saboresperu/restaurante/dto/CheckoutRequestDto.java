package com.saboresperu.restaurante.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckoutRequestDto(
        @NotNull Integer addressId,
        @NotNull Integer paymentMethodId,
        @Size(max = 500) String notes,
        @Size(max = 40) String couponCode
) {}
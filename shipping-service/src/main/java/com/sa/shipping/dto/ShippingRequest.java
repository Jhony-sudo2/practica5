package com.sa.shipping.dto;

import jakarta.validation.constraints.*;

public record ShippingRequest(
        @NotNull @Positive Long orderId, @NotBlank @Size(max = 250) String address) {}

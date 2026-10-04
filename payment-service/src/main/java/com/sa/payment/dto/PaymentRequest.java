package com.sa.payment.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull @Positive Long orderId,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount) {}

package com.sa.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public final class InventoryDtos {
    private InventoryDtos() {}

    public record Item(
            @NotNull @Positive Long productId, @NotNull @Min(1) @Max(1000) Integer quantity) {}

    public record Reserve(
            @NotNull @Positive Long orderId, @NotEmpty @Size(max = 20) List<@Valid Item> items) {}

    public record Release(@NotNull @Positive Long orderId) {}

    public record ProductInput(
            @NotBlank @Size(max = 200) String name,
            @NotNull @Min(0) Integer stock,
            @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal price) {}

    public record ReservationView(Long id, Long orderId, String status, List<Item> items) {}
}

package com.sa.order.dto;

import com.sa.order.models.OrderStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() {}

    public record Item(
            @NotNull @Positive Long productId, @NotNull @Min(1) @Max(1000) Integer quantity) {}

    public record CreateOrder(
            @NotBlank @Size(max = 100) String requestId,
            @NotNull @Positive Long customerId,
            @NotBlank @Size(max = 250) String address,
            @NotEmpty @Size(max = 20) List<@Valid Item> items) {}

    public record ItemView(Long productId, Integer quantity, BigDecimal unitPrice) {}

    public record OrderView(
            Long id,
            String requestId,
            Long customerId,
            String address,
            BigDecimal totalAmount,
            OrderStatus status,
            String failureReason,
            boolean technicalFailure,
            List<ItemView> items,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean paymentAttempted,
            boolean inventoryAttempted,
            boolean shippingAttempted,
            boolean paymentCompensated,
            boolean inventoryCompensated,
            boolean shippingCompensated) {}

    public record ProductView(Long id, String name, Integer stock, BigDecimal price) {}

    public record PaymentRequest(Long orderId, BigDecimal amount) {}

    public record PaymentView(
            Long id, Long orderId, BigDecimal amount, String status, String transactionReference) {}

    public record ReserveRequest(Long orderId, List<Item> items) {}

    public record ReleaseRequest(Long orderId) {}

    public record ReservationView(Long id, Long orderId, String status, List<Item> items) {}

    public record ShippingRequest(Long orderId, String address) {}

    public record ShipmentView(Long id, Long orderId, String address, String status) {}
}

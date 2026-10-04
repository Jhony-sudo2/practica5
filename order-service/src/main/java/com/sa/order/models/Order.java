package com.sa.order.models;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String requestId;

    @Column(nullable = false, length = 64)
    private String requestFingerprint;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, length = 250)
    private String address;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(length = 1000)
    private String failureReason;

    private boolean technicalFailure;
    private boolean paymentAttempted;
    private boolean inventoryAttempted;
    private boolean shippingAttempted;
    private boolean paymentCompensated;
    private boolean inventoryCompensated;
    private boolean shippingCompensated;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}

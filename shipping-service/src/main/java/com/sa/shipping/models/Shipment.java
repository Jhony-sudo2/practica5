package com.sa.shipping.models;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    private String address;

    @Column(nullable = false)
    private String status;

    private LocalDateTime scheduledDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

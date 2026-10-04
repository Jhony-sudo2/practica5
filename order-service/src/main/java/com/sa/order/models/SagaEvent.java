package com.sa.order.models;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "saga_events")
@Getter
@Setter
@NoArgsConstructor
public class SagaEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String action;

    @Column(length = 1000)
    private String detail;

    private LocalDateTime createdAt;
}

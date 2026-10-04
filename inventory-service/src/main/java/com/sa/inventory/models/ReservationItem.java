package com.sa.inventory.models;

import jakarta.persistence.*;

import lombok.*;


@Entity
@Table(name = "reservation_items")
@Getter
@Setter
@NoArgsConstructor
public class ReservationItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;
}

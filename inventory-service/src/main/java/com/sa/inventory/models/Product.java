package com.sa.inventory.models;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
}

package com.sa.inventory.controllers;

import com.sa.inventory.dto.InventoryDtos.*;
import com.sa.inventory.models.Product;
import com.sa.inventory.services.fault.FaultService;
import com.sa.inventory.services.productservice.ProductService;
import com.sa.inventory.services.reservation.ReservationService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InventoryController {
    private final ProductService products;
    private final ReservationService reservations;
    private final FaultService fault;

    @PostMapping("/products")
    public Product create(@Valid @RequestBody ProductInput r) {
        return products.create(r);
    }

    @GetMapping("/products")
    public List<Product> all() {
        return products.all();
    }

    @GetMapping("/products/{id}")
    public Product get(@PathVariable Long id) {
        fault.before(false);
        Product p = products.get(id);
        fault.after(false);
        return p;
    }

    @PostMapping("/inventory/reserve")
    public ReservationView reserve(@Valid @RequestBody Reserve r) {
        fault.before(false);
        ReservationView result = reservations.reserve(r);
        fault.after(false);
        return result;
    }

    @PostMapping("/inventory/release")
    public ReservationView release(@Valid @RequestBody Release r) {
        fault.before(true);
        ReservationView result = reservations.release(r.orderId());
        fault.after(true);
        return result;
    }

    @GetMapping("/inventory/reservations/by-order/{orderId}")
    public ReservationView reservation(@PathVariable Long orderId) {
        return reservations.byOrder(orderId);
    }
}

package com.sa.order.controllers;

import com.sa.order.dto.OrderDtos.*;
import com.sa.order.models.SagaEvent;
import com.sa.order.services.orderservice.OrderService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    @PostMapping
    public ResponseEntity<OrderView> create(@Valid @RequestBody CreateOrder request) {
        OrderView o = service.create(request);
        int status =
                switch (o.status()) {
                    case CONFIRMED -> 201;
                    case PENDING, COMPENSATING, COMPENSATION_PENDING -> 202;
                    case CANCELLED -> o.technicalFailure() ? 503 : 409;
                };
        return ResponseEntity.status(status).body(o);
    }

    @GetMapping
    public List<OrderView> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public OrderView get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/{id}/events")
    public List<SagaEvent> events(@PathVariable Long id) {
        return service.events(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderView> cancel(@PathVariable Long id) {
        OrderView o = service.cancel(id);
        return ResponseEntity.status(
                        o.status() == com.sa.order.models.OrderStatus.CANCELLED ? 200 : 202)
                .body(o);
    }
}

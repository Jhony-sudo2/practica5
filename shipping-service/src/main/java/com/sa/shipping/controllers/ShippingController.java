package com.sa.shipping.controllers;

import com.sa.shipping.dto.ShippingRequest;
import com.sa.shipping.models.Shipment;
import com.sa.shipping.services.impl.FaultService;
import com.sa.shipping.services.shipping.ShippingService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
public class ShippingController {
    private final ShippingService service;
    private final FaultService fault;

    @PostMapping("/schedule")
    public Shipment schedule(@Valid @RequestBody ShippingRequest r) {
        fault.before(false);
        Shipment s = service.schedule(r);
        fault.after(false);
        return s;
    }

    @DeleteMapping("/{id}")
    public Shipment cancel(@PathVariable Long id) {
        fault.before(true);
        Shipment s = service.cancel(id);
        fault.after(true);
        return s;
    }

    @DeleteMapping("/by-order/{orderId}")
    public Shipment cancelOrder(@PathVariable Long orderId) {
        fault.before(true);
        Shipment s = service.cancelOrder(orderId);
        fault.after(true);
        return s;
    }

    @GetMapping
    public List<Shipment> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public Shipment get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/by-order/{orderId}")
    public Shipment byOrder(@PathVariable Long orderId) {
        return service.byOrder(orderId);
    }
}

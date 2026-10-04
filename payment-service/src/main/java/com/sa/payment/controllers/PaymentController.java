package com.sa.payment.controllers;

import com.sa.payment.dto.PaymentRequest;
import com.sa.payment.models.Payment;
import com.sa.payment.services.fault.FaultService;
import com.sa.payment.services.payment.PaymentService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService service;
    private final FaultService fault;

    @PostMapping
    public Payment charge(@Valid @RequestBody PaymentRequest request) {
        fault.before(false);
        Payment p = service.charge(request);
        fault.after(false);
        return p;
    }

    @PostMapping("/{id}/refund")
    public Payment refund(@PathVariable Long id) {
        fault.before(true);
        Payment p = service.refund(id);
        fault.after(true);
        return p;
    }

    @PostMapping("/by-order/{orderId}/refund")
    public Payment refundOrder(@PathVariable Long orderId) {
        fault.before(true);
        Payment p = service.refundOrder(orderId);
        fault.after(true);
        return p;
    }

    @GetMapping
    public List<Payment> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public Payment get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/by-order/{orderId}")
    public Payment byOrder(@PathVariable Long orderId) {
        return service.getByOrder(orderId);
    }
}

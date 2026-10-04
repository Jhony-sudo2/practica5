package com.sa.order.client;

import com.sa.order.dto.OrderDtos.*;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "payment", url = "${services.payment.url}")
public interface PaymentClient {
    @PostMapping("/payments")
    PaymentView charge(@RequestBody PaymentRequest request);

    @PostMapping("/payments/by-order/{orderId}/refund")
    PaymentView refundOrder(@PathVariable("orderId") Long orderId);

    @GetMapping("/demo/ping")
    Map<String, String> ping();
}

package com.sa.order.client;

import com.sa.order.dto.OrderDtos.*;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "shipping", url = "${services.shipping.url}")
public interface ShippingClient {
    @PostMapping("/shipping/schedule")
    ShipmentView schedule(@RequestBody ShippingRequest request);

    @DeleteMapping("/shipping/by-order/{orderId}")
    ShipmentView cancelOrder(@PathVariable("orderId") Long orderId);

    @GetMapping("/demo/ping")
    Map<String, String> ping();
}

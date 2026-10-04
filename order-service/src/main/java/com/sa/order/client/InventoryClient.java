package com.sa.order.client;

import com.sa.order.dto.OrderDtos.*;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "inventory", url = "${services.inventory.url}")
public interface InventoryClient {
    @GetMapping("/products/{id}")
    ProductView product(@PathVariable("id") Long id);

    @PostMapping("/inventory/reserve")
    ReservationView reserve(@RequestBody ReserveRequest request);

    @PostMapping("/inventory/release")
    ReservationView release(@RequestBody ReleaseRequest request);

    @GetMapping("/demo/ping")
    Map<String, String> ping();
}

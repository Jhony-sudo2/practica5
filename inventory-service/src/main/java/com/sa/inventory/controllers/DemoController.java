package com.sa.inventory.controllers;

import com.sa.inventory.dto.FaultRequest;
import com.sa.inventory.services.fault.FaultService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/demo")
@ConditionalOnProperty(name = "demo.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DemoController {
    private final FaultService fault;

    @PutMapping("/fault")
    public FaultRequest set(@Valid @RequestBody FaultRequest request) {
        fault.configure(request);
        return fault.configuration();
    }

    @GetMapping("/fault")
    public FaultRequest get() {
        return fault.configuration();
    }

    @GetMapping("/ping")
    public Map<String, String> ping() {
        fault.before(false);
        fault.after(false);
        return Map.of("message", "OK");
    }
}

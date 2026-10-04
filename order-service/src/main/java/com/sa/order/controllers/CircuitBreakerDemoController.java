package com.sa.order.controllers;

import io.github.resilience4j.circuitbreaker.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import com.sa.order.services.remoteservice.RemoteService;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@RestController
@RequestMapping("/demo")
@ConditionalOnProperty(name = "demo.enabled", havingValue = "true")
public class CircuitBreakerDemoController {
    private final CircuitBreakerRegistry registry;
    private final RemoteService remote;
    private final Deque<Map<String, String>> transitions = new ConcurrentLinkedDeque<>();

    public CircuitBreakerDemoController(CircuitBreakerRegistry registry, RemoteService remote) {
        this.registry = registry;
        this.remote = remote;
        registry.getAllCircuitBreakers()
                .forEach(
                        cb ->
                                cb.getEventPublisher()
                                        .onStateTransition(
                                                e -> {
                                                    transitions.addLast(
                                                            Map.of(
                                                                    "name",
                                                                    e.getCircuitBreakerName(),
                                                                    "transition",
                                                                    e.getStateTransition()
                                                                            .toString(),
                                                                    "at",
                                                                    e.getCreationTime()
                                                                            .toString()));
                                                    while (transitions.size() > 100)
                                                        transitions.pollFirst();
                                                }));
    }

    @GetMapping("/probe/{service}")
    public Map<String, String> probe(@PathVariable String service) {
        return remote.probe(service);
    }

    @GetMapping("/circuit-breakers")
    public List<Map<String, Object>> states() {
        return registry.getAllCircuitBreakers().stream()
                .sorted(Comparator.comparing(CircuitBreaker::getName))
                .map(
                        cb -> {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("name", cb.getName());
                            m.put("state", cb.getState().name());
                            m.put("failureRate", cb.getMetrics().getFailureRate());
                            m.put("slowCallRate", cb.getMetrics().getSlowCallRate());
                            m.put("bufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
                            m.put("failedCalls", cb.getMetrics().getNumberOfFailedCalls());
                            m.put(
                                    "notPermittedCalls",
                                    cb.getMetrics().getNumberOfNotPermittedCalls());
                            return m;
                        })
                .toList();
    }

    @GetMapping("/circuit-breakers/events")
    public List<Map<String, String>> events() {
        return new ArrayList<>(transitions);
    }

    @PostMapping("/circuit-breakers/reset")
    public List<Map<String, Object>> reset() {
        registry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        transitions.clear();
        return states();
    }
}

package com.sa.order.config;

import com.sa.order.exceptions.BusinessException;

import io.github.resilience4j.circuitbreaker.*;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import java.util.List;

@Configuration
@EnableConfigurationProperties(BreakerProperties.class)
public class CircuitBreakerConfiguration {
    @Bean
    public CircuitBreakerRegistry registry(BreakerProperties p) {
        CircuitBreakerConfig config =
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(p.getFailureRateThreshold())
                        .slowCallRateThreshold(p.getSlowCallRateThreshold())
                        .slowCallDurationThreshold(p.getSlowCallDurationThreshold())
                        .waitDurationInOpenState(p.getWaitDurationInOpenState())
                        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                        .slidingWindowSize(p.getSlidingWindowSize())
                        .minimumNumberOfCalls(p.getMinimumNumberOfCalls())
                        .permittedNumberOfCallsInHalfOpenState(
                                p.getPermittedNumberOfCallsInHalfOpenState())
                        .automaticTransitionFromOpenToHalfOpenEnabled(
                                p.isAutomaticTransitionFromOpenToHalfOpenEnabled())
                        .ignoreExceptions(BusinessException.class)
                        .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        for (String name : List.of("paymentServiceCB", "inventoryServiceCB", "shippingServiceCB"))
            registry.circuitBreaker(name);
        return registry;
    }
}

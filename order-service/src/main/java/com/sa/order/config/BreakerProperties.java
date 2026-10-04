package com.sa.order.config;

import lombok.*;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "resilience4j.circuitbreaker.configs.default")
@Getter
@Setter
public class BreakerProperties {
    private float failureRateThreshold = 50;
    private float slowCallRateThreshold = 50;
    private Duration slowCallDurationThreshold = Duration.ofMillis(1500);
    private Duration waitDurationInOpenState = Duration.ofSeconds(10);
    private int slidingWindowSize = 10;
    private int minimumNumberOfCalls = 10;
    private int permittedNumberOfCallsInHalfOpenState = 3;
    private boolean automaticTransitionFromOpenToHalfOpenEnabled = true;
}

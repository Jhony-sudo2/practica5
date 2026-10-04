package com.sa.order;

import static org.assertj.core.api.Assertions.*;

import com.sa.order.config.*;
import com.sa.order.exceptions.BusinessException;

import io.github.resilience4j.circuitbreaker.*;

import org.junit.jupiter.api.Test;

import java.time.Duration;

class CircuitBreakerTest {
    @Test
    void closedOpenHalfOpenClosed() throws Exception {
        BreakerProperties p = new BreakerProperties();
        p.setWaitDurationInOpenState(Duration.ofMillis(20));
        p.setAutomaticTransitionFromOpenToHalfOpenEnabled(false);
        CircuitBreaker cb =
                new CircuitBreakerConfiguration().registry(p).circuitBreaker("paymentServiceCB");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        for (int i = 0; i < 10; i++) {
            try {
                cb.executeSupplier(
                        () -> {
                            throw new IllegalStateException("Down");
                        });
            } catch (IllegalStateException ignored) {
            }
        }
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThatThrownBy(() -> cb.executeSupplier(() -> "OK"))
                .isInstanceOf(CallNotPermittedException.class);
        Thread.sleep(30);
        cb.executeSupplier(() -> "OK");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
        cb.executeSupplier(() -> "OK");
        cb.executeSupplier(() -> "OK");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void businessRejectionsDoNotOpenTheCircuit() {
        CircuitBreaker cb =
                new CircuitBreakerConfiguration()
                        .registry(new BreakerProperties())
                        .circuitBreaker("inventoryServiceCB");
        for (int i = 0; i < 20; i++) {
            try {
                cb.executeSupplier(
                        () -> {
                            throw new BusinessException(409, "Sin stock");
                        });
            } catch (BusinessException ignored) {
            }
        }
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(cb.getMetrics().getNumberOfFailedCalls()).isZero();
    }

    @Test
    void twoDependenciesAreIsolated() {
        var registry = new CircuitBreakerConfiguration().registry(new BreakerProperties());
        registry.circuitBreaker("paymentServiceCB").transitionToOpenState();
        assertThat(registry.circuitBreaker("inventoryServiceCB").getState())
                .isEqualTo(CircuitBreaker.State.CLOSED);
    }
}

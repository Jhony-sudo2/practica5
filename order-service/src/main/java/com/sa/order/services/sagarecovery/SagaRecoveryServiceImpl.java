package com.sa.order.services.sagarecovery;

import com.sa.order.services.ordersagaservice.OrderSagaService;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SagaRecoveryServiceImpl implements SagaRecoveryService {
    private final OrderSagaService saga;

    @Scheduled(
            fixedDelayString = "${saga.recovery-delay-ms:5000}",
            initialDelayString = "${saga.recovery-delay-ms:5000}")
    public void recoverPending() {
        saga.recover();
    }
}

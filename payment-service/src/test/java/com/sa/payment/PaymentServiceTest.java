package com.sa.payment;

import static org.assertj.core.api.Assertions.*;

import com.sa.payment.dto.PaymentRequest;
import com.sa.payment.exceptions.BusinessException;
import com.sa.payment.repositories.PaymentRepository;
import com.sa.payment.services.payment.PaymentService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:paymenttests;MODE=MySQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "demo.enabled=false",
            "saga.recovery-delay-ms=3600000"
        })
public class PaymentServiceTest {
    @Autowired PaymentService service;
    @Autowired PaymentRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void chargeAndRefundAreIdempotent() {
        PaymentRequest r = new PaymentRequest(10L, new BigDecimal("125.00"));
        Long id = service.charge(r).getId();
        assertThat(service.charge(r).getId()).isEqualTo(id);
        service.refund(id);
        service.refund(id);
        assertThat(repository.count()).isEqualTo(1);
        assertThat(service.get(id).getStatus()).isEqualTo("REFUNDED");
        assertThatThrownBy(() -> service.charge(r)).isInstanceOf(BusinessException.class);
    }

    @Test
    void unknownRefundPreventsLateCharge() {
        service.refundOrder(20L);
        assertThatThrownBy(() -> service.charge(new PaymentRequest(20L, new BigDecimal("10.00"))))
                .isInstanceOf(BusinessException.class);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void differentAmountIsRejected() {
        service.charge(new PaymentRequest(30L, new BigDecimal("10.00")));
        assertThatThrownBy(() -> service.charge(new PaymentRequest(30L, new BigDecimal("20.00"))))
                .isInstanceOf(BusinessException.class);
    }
}

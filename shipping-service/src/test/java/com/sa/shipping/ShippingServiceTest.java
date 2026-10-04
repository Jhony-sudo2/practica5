package com.sa.shipping;

import static org.assertj.core.api.Assertions.*;

import com.sa.shipping.dto.ShippingRequest;
import com.sa.shipping.exceptions.BusinessException;
import com.sa.shipping.repositories.ShipmentRepository;
import com.sa.shipping.services.shipping.ShippingService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:shippingtests;MODE=MySQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "demo.enabled=false",
            "saga.recovery-delay-ms=3600000"
        })
public class ShippingServiceTest {
    @Autowired ShippingService service;
    @Autowired ShipmentRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void scheduleAndCancelAreIdempotent() {
        ShippingRequest r = new ShippingRequest(10L, "Quetzaltenango");
        Long id = service.schedule(r).getId();
        assertThat(service.schedule(r).getId()).isEqualTo(id);
        service.cancel(id);
        service.cancel(id);
        assertThat(repository.count()).isEqualTo(1);
        assertThat(service.get(id).getStatus()).isEqualTo("CANCELLED");
        assertThatThrownBy(() -> service.schedule(r)).isInstanceOf(BusinessException.class);
    }

    @Test
    void cancellationPreventsLateSchedule() {
        service.cancelOrder(20L);
        assertThatThrownBy(() -> service.schedule(new ShippingRequest(20L, "Guatemala")))
                .isInstanceOf(BusinessException.class);
    }
}

package com.sa.inventory;

import static org.assertj.core.api.Assertions.*;

import com.sa.inventory.dto.InventoryDtos.*;
import com.sa.inventory.exceptions.BusinessException;
import com.sa.inventory.repositories.*;
import com.sa.inventory.services.productservice.ProductService;
import com.sa.inventory.services.reservation.ReservationService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:inventorytests;MODE=MySQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "demo.enabled=false",
            "saga.recovery-delay-ms=3600000"
        })
public class InventoryServiceTest {
    @Autowired ProductService products;
    @Autowired ReservationService reservations;
    @Autowired ProductRepository productRepository;
    @Autowired ReservationRepository reservationRepository;
    private Long available, empty;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        productRepository.deleteAll();
        available =
                products.create(new ProductInput("Disponible", 10, new BigDecimal("10.00")))
                        .getId();
        empty = products.create(new ProductInput("Agotado", 0, new BigDecimal("20.00"))).getId();
    }

    @Test
    void partialCartFailureDoesNotDiscountAnything() {
        assertThatThrownBy(
                        () ->
                                reservations.reserve(
                                        new Reserve(
                                                1L,
                                                List.of(
                                                        new Item(available, 2),
                                                        new Item(empty, 1)))))
                .isInstanceOf(BusinessException.class);
        assertThat(products.get(available).getStock()).isEqualTo(10);
        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    void reserveAndReleaseAreIdempotent() {
        Reserve r = new Reserve(2L, List.of(new Item(available, 3)));
        Long id = reservations.reserve(r).id();
        assertThat(reservations.reserve(r).id()).isEqualTo(id);
        assertThat(products.get(available).getStock()).isEqualTo(7);
        reservations.release(2L);
        reservations.release(2L);
        assertThat(products.get(available).getStock()).isEqualTo(10);
        assertThatThrownBy(() -> reservations.reserve(r)).isInstanceOf(BusinessException.class);
    }

    @Test
    void releaseBeforeReservePreventsLateEffect() {
        reservations.release(3L);
        assertThatThrownBy(
                        () ->
                                reservations.reserve(
                                        new Reserve(3L, List.of(new Item(available, 1)))))
                .isInstanceOf(BusinessException.class);
        assertThat(products.get(available).getStock()).isEqualTo(10);
    }

    @Test
    void concurrentOrdersCannotOversell() throws Exception {
        var executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            var a =
                    executor.submit(
                            () -> {
                                try {
                                    reservations.reserve(
                                            new Reserve(4L, List.of(new Item(available, 7))));
                                    return true;
                                } catch (BusinessException e) {
                                    return false;
                                }
                            });
            var b =
                    executor.submit(
                            () -> {
                                try {
                                    reservations.reserve(
                                            new Reserve(5L, List.of(new Item(available, 7))));
                                    return true;
                                } catch (BusinessException e) {
                                    return false;
                                }
                            });
            assertThat(List.of(a.get(), b.get())).containsExactlyInAnyOrder(true, false);
            assertThat(products.get(available).getStock()).isEqualTo(3);
        } finally {
            executor.shutdownNow();
        }
    }
}

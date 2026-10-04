package com.sa.order;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sa.order.dto.OrderDtos.*;
import com.sa.order.exceptions.BusinessException;
import com.sa.order.models.*;
import com.sa.order.repositories.*;
import com.sa.order.services.ordersagaservice.OrderSagaService;
import com.sa.order.services.orderservice.OrderService;
import com.sa.order.services.remoteservice.RemoteService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.*;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:ordertests;MODE=MySQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "demo.enabled=false",
            "saga.recovery-delay-ms=3600000"
        })
public class OrderSagaTest {
    @Autowired OrderService orders;
    @Autowired OrderSagaService saga;
    @Autowired OrderRepository repository;
    @Autowired SagaEventRepository eventRepository;
    @MockitoBean RemoteService remote;

    @BeforeEach
    void setup() {
        eventRepository.deleteAll();
        repository.deleteAll();
        when(remote.product(1L))
                .thenReturn(new ProductView(1L, "Teclado", 10, new BigDecimal("50.00")));
    }

    private CreateOrder request() {
        return new CreateOrder(
                UUID.randomUUID().toString(), 1L, "Quetzaltenango", List.of(new Item(1L, 2)));
    }

    @Test
    void successfulSagaAndDuplicateRequest() {
        CreateOrder r = request();
        OrderView o = orders.create(r);
        assertThat(o.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(o.totalAmount()).isEqualByComparingTo("100.00");
        assertThat(orders.create(r).id()).isEqualTo(o.id());
        verify(remote, times(1)).pay(o.id(), new BigDecimal("100.00"));
        verify(remote, never()).refund(anyLong());
        assertThat(orders.events(o.id()))
                .extracting(SagaEvent::getAction)
                .contains("ORDER_CREATED", "ORDER_CONFIRMED");
    }

    @Test
    void inventoryFailureRefundsAndCancels() {
        when(remote.reserve(anyLong(), anyList()))
                .thenThrow(new BusinessException(409, "Sin stock"));
        OrderView o = orders.create(request());
        assertThat(o.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(o.technicalFailure()).isFalse();
        verify(remote).refund(o.id());
        verify(remote).release(o.id());
        verify(remote, never()).schedule(anyLong(), anyString());
        assertThat(o.paymentCompensated()).isTrue();
    }

    @Test
    void shippingFailureCompensatesInReverseOrder() {
        when(remote.schedule(anyLong(), anyString()))
                .thenThrow(new BusinessException(503, "Servicio caído"));
        OrderView o = orders.create(request());
        assertThat(o.status()).isEqualTo(OrderStatus.CANCELLED);
        var inOrder = inOrder(remote);
        inOrder.verify(remote).cancelShipment(o.id());
        inOrder.verify(remote).release(o.id());
        inOrder.verify(remote).refund(o.id());
    }

    @Test
    void uncertainPaymentIsRefundedEvenWithoutResponse() {
        when(remote.pay(anyLong(), any()))
                .thenThrow(new BusinessException(503, "Timeout después de cobrar"));
        OrderView o = orders.create(request());
        assertThat(o.status()).isEqualTo(OrderStatus.CANCELLED);
        verify(remote).refund(o.id());
        verify(remote, never()).reserve(anyLong(), anyList());
    }

    @Test
    void failedCompensationRemainsPendingAndCanRecover() {
        when(remote.reserve(anyLong(), anyList()))
                .thenThrow(new BusinessException(409, "Sin stock"));
        doThrow(new RuntimeException("Payment caído")).doNothing().when(remote).refund(anyLong());
        OrderView o = orders.create(request());
        assertThat(o.status()).isEqualTo(OrderStatus.COMPENSATION_PENDING);
        assertThat(o.paymentCompensated()).isFalse();
        saga.recover();
        assertThat(orders.get(o.id()).status()).isEqualTo(OrderStatus.CANCELLED);
        verify(remote, times(2)).refund(o.id());
        verify(remote, times(1)).release(o.id());
    }

    @Test
    void sameKeyWithDifferentCartIsRejected() {
        CreateOrder r = request();
        orders.create(r);
        assertThatThrownBy(
                        () ->
                                orders.create(
                                        new CreateOrder(
                                                r.requestId(),
                                                r.customerId(),
                                                r.address(),
                                                List.of(new Item(1L, 3)))))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void cancellingConfirmedOrderCompensatesAllSteps() {
        OrderView o = orders.create(request());
        orders.cancel(o.id());
        orders.cancel(o.id());
        verify(remote, times(1)).cancelShipment(o.id());
        verify(remote, times(1)).release(o.id());
        verify(remote, times(1)).refund(o.id());
    }
}

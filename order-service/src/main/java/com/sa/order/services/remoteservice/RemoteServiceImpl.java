package com.sa.order.services.remoteservice;

import com.sa.order.client.*;
import com.sa.order.dto.OrderDtos.*;
import com.sa.order.exceptions.BusinessException;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RemoteServiceImpl implements RemoteService {
    private final PaymentClient payment;
    private final InventoryClient inventory;
    private final ShippingClient shipping;
    private final CircuitBreakerRegistry registry;

    public ProductView product(Long id) {
        return guarded("inventoryServiceCB", () -> inventory.product(id));
    }

    public PaymentView pay(Long id, BigDecimal total) {
        return guarded("paymentServiceCB", () -> payment.charge(new PaymentRequest(id, total)));
    }

    public ReservationView reserve(Long id, List<Item> items) {
        return guarded(
                "inventoryServiceCB", () -> inventory.reserve(new ReserveRequest(id, items)));
    }

    public ShipmentView schedule(Long id, String address) {
        return guarded(
                "shippingServiceCB", () -> shipping.schedule(new ShippingRequest(id, address)));
    }

    // Compensaciones independientes de los breakers de avance: un circuito OPEN no debe impedir
    // deshacer.
    public void refund(Long id) {
        payment.refundOrder(id);
    }

    public void release(Long id) {
        inventory.release(new ReleaseRequest(id));
    }

    public void cancelShipment(Long id) {
        shipping.cancelOrder(id);
    }

    public Map<String, String> probe(String svc) {
        return switch (svc) {
            case "payment" -> guarded("paymentServiceCB", payment::ping);
            case "inventory" -> guarded("inventoryServiceCB", inventory::ping);
            case "shipping" -> guarded("shippingServiceCB", shipping::ping);
            default -> throw new BusinessException(404, "Servicio desconocido");
        };
    }

    private <T> T guarded(String name, Supplier<T> call) {
        try {
            return registry.circuitBreaker(name).executeSupplier(call);
        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new BusinessException(
                    503,
                    "Servicio no disponible, intente más tarde ("
                            + name
                            + "): "
                            + e.getClass().getSimpleName());
        }
    }
}

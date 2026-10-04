package com.sa.order.services.ordersagaservice;

import com.sa.order.dto.OrderDtos.*;
import com.sa.order.exceptions.BusinessException;
import com.sa.order.models.*;
import com.sa.order.services.orderstore.OrderStoreService;
import com.sa.order.services.remoteservice.RemoteService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderSagaServiceImpl implements OrderSagaService {
    private final OrderStoreService store;
    private final RemoteService remote;

    // PoC: una instancia del orquestador. Serializa ejecución/cancelación/recuperación para no
    // competir entre sí.
    public synchronized OrderView run(Long id) {
        OrderView o = store.get(id);
        if (o.status() != OrderStatus.PENDING) return o;
        try {
            store.attempted(id, SagaStep.PAYMENT);
            remote.pay(id, o.totalAmount());
            store.completed(id, SagaStep.PAYMENT);
            store.attempted(id, SagaStep.INVENTORY);
            remote.reserve(
                    id,
                    o.items().stream().map(i -> new Item(i.productId(), i.quantity())).toList());
            store.completed(id, SagaStep.INVENTORY);
            store.attempted(id, SagaStep.SHIPPING);
            remote.schedule(id, o.address());
            store.completed(id, SagaStep.SHIPPING);
            store.confirmed(id);
        } catch (Exception e) {
            boolean technical = !(e instanceof BusinessException b) || b.getStatus() >= 500;
            store.failure(id, e.getMessage(), technical);
            compensate(id);
        }
        return store.get(id);
    }

    public synchronized OrderView cancel(Long id) {
        OrderView o = store.get(id);
        if (o.status() == OrderStatus.CANCELLED) return o;
        store.failure(id, "Cancelación solicitada por el cliente", false);
        compensate(id);
        return store.get(id);
    }

    public synchronized void recover() {
        for (Long id : store.recoverableIds()) {
            try {
                OrderView o = store.get(id);
                if (o.status() == OrderStatus.PENDING)
                    store.failure(
                            id,
                            "Saga interrumpida o estancada; recuperación por compensación",
                            true);
                compensate(id);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(getClass())
                        .error("Recuperación pendiente order={}", id, e);
            }
        }
    }

    private void compensate(Long id) {
        store.compensationState(id, OrderStatus.COMPENSATING);
        OrderView o = store.get(id);
        boolean ok = true;
        if (o.shippingAttempted() && !o.shippingCompensated())
            ok = undo(id, SagaStep.SHIPPING, () -> remote.cancelShipment(id)) && ok;
        if (o.inventoryAttempted() && !o.inventoryCompensated())
            ok = undo(id, SagaStep.INVENTORY, () -> remote.release(id)) && ok;
        if (o.paymentAttempted() && !o.paymentCompensated())
            ok = undo(id, SagaStep.PAYMENT, () -> remote.refund(id)) && ok;
        store.compensationState(id, ok ? OrderStatus.CANCELLED : OrderStatus.COMPENSATION_PENDING);
    }

    private boolean undo(Long id, SagaStep step, Runnable action) {
        try {
            action.run();
            store.compensated(id, step);
            return true;
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                    .warn(
                            "Compensación pendiente order={} step={} error={}",
                            id,
                            step,
                            e.toString());
            return false;
        }
    }
}

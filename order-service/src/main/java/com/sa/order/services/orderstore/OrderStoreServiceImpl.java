package com.sa.order.services.orderstore;

import com.sa.order.dto.OrderDtos.*;
import com.sa.order.exceptions.BusinessException;
import com.sa.order.models.*;
import com.sa.order.repositories.*;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderStoreServiceImpl implements OrderStoreService {
    private final OrderRepository orderRepository;
    private final SagaEventRepository eventsRepository;

    @Value("${saga.stale-after-seconds:120}")
    private long staleSeconds;

    @Transactional(readOnly = true)
    public OrderView existing(CreateOrder request) {
        Order o = orderRepository.findByRequestId(request.requestId()).orElse(null);
        if (o == null) return null;
        if (!o.getRequestFingerprint().equals(fingerprint(request)))
            throw new BusinessException(409, "requestId reutilizado con datos diferentes");
        return view(o);
    }

    @Transactional
    public OrderView create(CreateOrder r, List<ItemView> items) {
        Order o = new Order();
        o.setRequestId(r.requestId());
        o.setRequestFingerprint(fingerprint(r));
        o.setCustomerId(r.customerId());
        o.setAddress(r.address());
        o.setStatus(OrderStatus.PENDING);
        o.setCreatedAt(LocalDateTime.now());
        o.setUpdatedAt(o.getCreatedAt());
        BigDecimal total = BigDecimal.ZERO;
        for (ItemView i : items) {
            OrderItem line = new OrderItem();
            line.setOrder(o);
            line.setProductId(i.productId());
            line.setQuantity(i.quantity());
            line.setUnitPrice(i.unitPrice());
            o.getItems().add(line);
            total = total.add(i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())));
        }
        o.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        orderRepository.saveAndFlush(o);
        event(o.getId(), "ORDER_CREATED", "Orden pendiente; precios consultados en Inventory");
        return view(o);
    }

    @Transactional(readOnly = true)
    public OrderView get(Long id) {
        return view(load(id));
    }

    @Transactional(readOnly = true)
    public List<OrderView> all() {
        return orderRepository.findAll().stream().map(this::view).toList();
    }

    public List<SagaEvent> events(Long id) {
        return eventsRepository.findByOrderIdOrderByIdAsc(id);
    }

    @Transactional
    public void attempted(Long id, SagaStep step) {
        Order o = load(id);
        switch (step) {
            case PAYMENT -> o.setPaymentAttempted(true);
            case INVENTORY -> o.setInventoryAttempted(true);
            case SHIPPING -> o.setShippingAttempted(true);
        }
        touch(o);
        event(id, step + "_ATTEMPTED", "Marcado durable antes de la llamada HTTP");
    }

    @Transactional
    public void completed(Long id, SagaStep step) {
        touch(load(id));
        event(id, step + "_COMPLETED", "Respuesta exitosa del participante");
    }

    @Transactional
    public void compensated(Long id, SagaStep step) {
        Order o = load(id);
        switch (step) {
            case PAYMENT -> o.setPaymentCompensated(true);
            case INVENTORY -> o.setInventoryCompensated(true);
            case SHIPPING -> o.setShippingCompensated(true);
        }
        touch(o);
        event(id, step + "_COMPENSATED", "Compensación confirmada");
    }

    @Transactional
    public void confirmed(Long id) {
        Order o = load(id);
        o.setStatus(OrderStatus.CONFIRMED);
        touch(o);
        event(id, "ORDER_CONFIRMED", "Saga completada");
    }

    @Transactional
    public void failure(Long id, String message, boolean technical) {
        Order o = load(id);
        o.setFailureReason(shorten(message));
        o.setTechnicalFailure(technical);
        o.setStatus(OrderStatus.COMPENSATING);
        touch(o);
        event(id, "SAGA_FAILED", message);
    }

    @Transactional
    public void compensationState(Long id, OrderStatus status) {
        Order o = load(id);
        o.setStatus(status);
        touch(o);
        event(
                id,
                status.name(),
                status == OrderStatus.CANCELLED
                        ? "Todas las compensaciones necesarias confirmadas"
                        : "Se reintentará automáticamente si quedan compensaciones");
    }

    @Transactional(readOnly = true)
    public List<Long> recoverableIds() {
        List<Long> ids =
                new ArrayList<>(
                        orderRepository
                                .findByStatusIn(
                                        List.of(
                                                OrderStatus.COMPENSATING,
                                                OrderStatus.COMPENSATION_PENDING))
                                .stream()
                                .map(Order::getId)
                                .toList());
        ids.addAll(
                orderRepository
                        .findByStatusAndUpdatedAtBefore(
                                OrderStatus.PENDING, LocalDateTime.now().minusSeconds(staleSeconds))
                        .stream()
                        .map(Order::getId)
                        .toList());
        return ids;
    }

    private Order load(Long id) {
        return orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new BusinessException(404, "Orden no encontrada"));
    }

    private void touch(Order o) {
        o.setUpdatedAt(LocalDateTime.now());
    }

    private void event(Long id, String action, String detail) {
        SagaEvent e = new SagaEvent();
        e.setOrderId(id);
        e.setAction(action);
        e.setDetail(shorten(detail));
        e.setCreatedAt(LocalDateTime.now());
        eventsRepository.save(e);
        org.slf4j.LoggerFactory.getLogger(getClass())
                .info("SAGA order={} action={} detail={}", id, action, shorten(detail));
    }

    private String shorten(String s) {
        return s == null ? "" : s.substring(0, Math.min(s.length(), 1000));
    }

    private String fingerprint(CreateOrder r) {
        String canonical =
                r.customerId()
                        + "|"
                        + r.address().length()
                        + ":"
                        + r.address()
                        + "|"
                        + r.items().stream()
                                .sorted(Comparator.comparing(Item::productId))
                                .map(i -> i.productId() + ":" + i.quantity())
                                .collect(java.util.stream.Collectors.joining(","));
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private OrderView view(Order o) {
        return new OrderView(
                o.getId(),
                o.getRequestId(),
                o.getCustomerId(),
                o.getAddress(),
                o.getTotalAmount(),
                o.getStatus(),
                o.getFailureReason(),
                o.isTechnicalFailure(),
                o.getItems().stream()
                        .map(i -> new ItemView(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                        .toList(),
                o.getCreatedAt(),
                o.getUpdatedAt(),
                o.isPaymentAttempted(),
                o.isInventoryAttempted(),
                o.isShippingAttempted(),
                o.isPaymentCompensated(),
                o.isInventoryCompensated(),
                o.isShippingCompensated());
    }
}

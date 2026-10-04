package com.sa.order.services.orderservice;

import com.sa.order.dto.OrderDtos.*;
import com.sa.order.exceptions.BusinessException;
import com.sa.order.models.SagaEvent;
import com.sa.order.services.ordersagaservice.OrderSagaService;
import com.sa.order.services.orderstore.OrderStoreService;
import com.sa.order.services.remoteservice.RemoteService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderStoreService store;
    private final OrderSagaService saga;
    private final RemoteService remote;

    public OrderView create(CreateOrder request) {
        Set<Long> seen = new HashSet<>();
        for (Item i : request.items())
            if (!seen.add(i.productId()))
                throw new BusinessException(400, "Producto duplicado en el carrito");
        OrderView previous = store.existing(request);
        if (previous != null) return previous;
        List<ItemView> lines = new ArrayList<>();
        for (Item item : request.items()) {
            ProductView p = remote.product(item.productId());
            lines.add(new ItemView(item.productId(), item.quantity(), p.price()));
        }
        OrderView created = store.create(request, lines);
        return saga.run(created.id());
    }

    public OrderView get(Long id) {
        return store.get(id);
    }

    public List<OrderView> all() {
        return store.all();
    }

    public List<SagaEvent> events(Long id) {
        store.get(id);
        return store.events(id);
    }

    public OrderView cancel(Long id) {
        return saga.cancel(id);
    }
}

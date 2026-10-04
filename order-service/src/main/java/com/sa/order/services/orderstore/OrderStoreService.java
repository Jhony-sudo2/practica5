package com.sa.order.services.orderstore;

import com.sa.order.dto.OrderDtos.*;
import com.sa.order.models.*;
import java.util.*;

public interface OrderStoreService {
    OrderView existing(CreateOrder request);

    OrderView create(CreateOrder request, List<ItemView> items);

    OrderView get(Long id);

    List<OrderView> all();

    List<SagaEvent> events(Long id);

    void attempted(Long id, SagaStep step);

    void completed(Long id, SagaStep step);

    void compensated(Long id, SagaStep step);

    void confirmed(Long id);

    void failure(Long id, String message, boolean technical);

    void compensationState(Long id, OrderStatus status);

    List<Long> recoverableIds();
}

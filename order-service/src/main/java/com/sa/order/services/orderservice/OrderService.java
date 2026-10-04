package com.sa.order.services.orderservice;

import com.sa.order.dto.OrderDtos.*;
import com.sa.order.models.SagaEvent;

import java.util.*;

public interface OrderService {
    OrderView create(CreateOrder request);

    OrderView get(Long id);

    List<OrderView> all();

    List<SagaEvent> events(Long id);

    OrderView cancel(Long id);
}

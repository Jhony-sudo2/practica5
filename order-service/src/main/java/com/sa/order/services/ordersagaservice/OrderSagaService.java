package com.sa.order.services.ordersagaservice;

import com.sa.order.dto.OrderDtos.*;

public interface OrderSagaService {
    OrderView run(Long id);

    OrderView cancel(Long id);

    void recover();
}

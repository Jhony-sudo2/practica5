package com.sa.order.services.faultservice;

import com.sa.order.dto.FaultRequest;

public interface FaultService {
    void configure(FaultRequest request);

    FaultRequest configuration();

    void before(boolean compensation);

    void after(boolean compensation);
}

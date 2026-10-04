package com.sa.shipping.services.impl;

import com.sa.shipping.dto.FaultRequest;

public interface FaultService {
    void configure(FaultRequest request);

    FaultRequest configuration();

    void before(boolean compensation);

    void after(boolean compensation);
}

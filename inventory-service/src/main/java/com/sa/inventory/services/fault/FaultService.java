package com.sa.inventory.services.fault;

import com.sa.inventory.dto.FaultRequest;

public interface FaultService {
    void configure(FaultRequest request);

    FaultRequest configuration();

    void before(boolean compensation);

    void after(boolean compensation);
}

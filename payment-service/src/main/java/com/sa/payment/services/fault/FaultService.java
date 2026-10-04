package com.sa.payment.services.fault;

import com.sa.payment.dto.FaultRequest;

public interface FaultService {
    void configure(FaultRequest request);

    FaultRequest configuration();

    void before(boolean compensation);

    void after(boolean compensation);
}

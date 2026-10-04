package com.sa.order.services.faultservice;

import com.sa.order.dto.FaultRequest;
import com.sa.order.exceptions.BusinessException;

import org.springframework.stereotype.Service;

@Service
public class FaultServiceImpl implements FaultService {
    private volatile FaultRequest current =
            new FaultRequest(FaultRequest.Mode.NORMAL, FaultRequest.Scope.FORWARD, 0);

    public void configure(FaultRequest request) {
        current = request;
    }

    public FaultRequest configuration() {
        return current;
    }

    private boolean applies(FaultRequest f, boolean compensation) {
        return f.scope() == FaultRequest.Scope.ALL
                || (compensation
                        ? f.scope() == FaultRequest.Scope.COMPENSATION
                        : f.scope() == FaultRequest.Scope.FORWARD);
    }

    public void before(boolean compensation) {
        FaultRequest f = current;
        if (!applies(f, compensation)) return;
        if (f.mode() == FaultRequest.Mode.ERROR)
            throw new BusinessException(503, "Fallo técnico simulado antes de guardar");
        if (f.mode() == FaultRequest.Mode.SLOW) pause(f.delayMs());
    }

    public void after(boolean compensation) {
        FaultRequest f = current;
        if (!applies(f, compensation)) return;
        if (f.mode() == FaultRequest.Mode.ERROR_AFTER_COMMIT)
            throw new BusinessException(503, "Fallo técnico simulado después del commit");
        if (f.mode() == FaultRequest.Mode.SLOW_AFTER_COMMIT) pause(f.delayMs());
    }

    private void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(503, "Operación interrumpida");
        }
    }
}

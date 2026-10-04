package com.sa.order.dto;

import jakarta.validation.constraints.*;

public record FaultRequest(
        @NotNull Mode mode, @NotNull Scope scope, @Min(0) @Max(15000) long delayMs) {
    public enum Mode {
        NORMAL,
        ERROR,
        SLOW,
        ERROR_AFTER_COMMIT,
        SLOW_AFTER_COMMIT
    }

    public enum Scope {
        FORWARD,
        COMPENSATION,
        ALL
    }
}

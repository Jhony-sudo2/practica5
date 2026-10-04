package com.sa.order.config;

import com.sa.order.exceptions.BusinessException;

import feign.Retryer;
import feign.codec.ErrorDecoder;

import org.springframework.context.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FeignConfiguration {
    @Bean
    Retryer retryer() {
        return Retryer.NEVER_RETRY;
    }

    @Bean
    ErrorDecoder errorDecoder() {
        ErrorDecoder fallback = new ErrorDecoder.Default();
        return (key, response) -> {
            if (response.status() >= 400 && response.status() < 500) {
                String message = "Rechazo del servicio (HTTP " + response.status() + ")";
                try {
                    if (response.body() != null) {
                        String detail =
                                new String(
                                        response.body().asInputStream().readAllBytes(),
                                        StandardCharsets.UTF_8);
                        message += ": " + detail.substring(0, Math.min(detail.length(), 500));
                    }
                } catch (IOException ignored) {
                }
                return new BusinessException(response.status(), message);
            }
            return fallback.decode(key, response);
        };
    }
}

package com.sa.order.controllers;

import com.sa.order.exceptions.BusinessException;

import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> business(BusinessException e) {
        return error(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        HttpMessageNotReadableException.class,
        ConstraintViolationException.class
    })
    public ResponseEntity<Map<String, Object>> invalid(Exception e) {
        return error(400, "Solicitud inválida: revise campos, tipos y cantidades positivas.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> conflict(Exception e) {
        return error(
                409,
                "Conflicto de concurrencia o clave duplicada; consulte el estado y reintente con la"
                    + " misma clave.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> unexpected(Exception e) {
        org.slf4j.LoggerFactory.getLogger(getClass()).error("Fallo inesperado", e);
        return error(500, "Error interno del servicio");
    }

    private ResponseEntity<Map<String, Object>> error(int code, String message) {
        return ResponseEntity.status(code)
                .body(
                        Map.of(
                                "status",
                                code,
                                "message",
                                message,
                                "timestamp",
                                Instant.now().toString()));
    }
}

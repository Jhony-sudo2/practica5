package com.sa.payment.services.payment;

import com.sa.payment.dto.PaymentRequest;
import com.sa.payment.exceptions.BusinessException;
import com.sa.payment.models.Payment;
import com.sa.payment.repositories.PaymentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository repository;

    @Transactional
    public Payment charge(PaymentRequest request) {
        Payment existing = repository.lockByOrderId(request.orderId()).orElse(null);
        if (existing != null) {
            if (!existing.getStatus().equals("COMPLETED"))
                throw new BusinessException(
                        409, "Pago ya reembolsado; esta orden no puede cobrarse de nuevo");
            if (existing.getAmount().compareTo(request.amount()) != 0)
                throw new BusinessException(409, "Monto diferente para la misma orden");
            return existing;
        }
        Payment p = new Payment();
        p.setOrderId(request.orderId());
        p.setAmount(request.amount());
        p.setStatus("COMPLETED");
        p.setTransactionReference(UUID.randomUUID().toString());
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(p.getCreatedAt());
        return repository.saveAndFlush(p);
    }

    @Transactional
    public Payment refund(Long id) {
        Long orderId = get(id).getOrderId();
        return refundOrder(orderId);
    }

    @Transactional
    public Payment refundOrder(Long orderId) {
        Payment p =
                repository
                        .lockByOrderId(orderId)
                        .orElseGet(
                                () -> {
                                    Payment n = new Payment();
                                    n.setOrderId(orderId);
                                    n.setAmount(BigDecimal.ZERO);
                                    n.setCreatedAt(LocalDateTime.now());
                                    return n;
                                });
        p.setStatus("REFUNDED");
        p.setUpdatedAt(LocalDateTime.now());
        return repository.saveAndFlush(p);
    }

    public Payment get(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new BusinessException(404, "Pago no encontrado"));
    }

    public Payment getByOrder(Long id) {
        return repository
                .findByOrderId(id)
                .orElseThrow(() -> new BusinessException(404, "Pago no encontrado"));
    }

    public List<Payment> all() {
        return repository.findAll();
    }
}

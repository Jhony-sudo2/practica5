package com.sa.payment.services.payment;

import com.sa.payment.dto.PaymentRequest;
import com.sa.payment.models.Payment;

import java.util.*;

public interface PaymentService {
    Payment charge(PaymentRequest request);

    Payment refund(Long id);

    Payment refundOrder(Long orderId);

    Payment get(Long id);

    Payment getByOrder(Long orderId);

    List<Payment> all();
}

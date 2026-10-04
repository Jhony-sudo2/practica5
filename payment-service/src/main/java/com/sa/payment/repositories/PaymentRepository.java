package com.sa.payment.repositories;

import com.sa.payment.models.Payment;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.orderId=:orderId")
    Optional<Payment> lockByOrderId(@Param("orderId") Long orderId);
}

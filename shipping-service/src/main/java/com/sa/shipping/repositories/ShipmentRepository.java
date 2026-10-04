package com.sa.shipping.repositories;

import com.sa.shipping.models.Shipment;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByOrderId(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Shipment s where s.orderId=:orderId")
    Optional<Shipment> lockByOrderId(@Param("orderId") Long orderId);
}

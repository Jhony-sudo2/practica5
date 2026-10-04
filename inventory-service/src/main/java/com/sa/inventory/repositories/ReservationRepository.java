package com.sa.inventory.repositories;

import com.sa.inventory.models.Reservation;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @EntityGraph(attributePaths = "items")
    Optional<Reservation> findByOrderId(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.orderId=:orderId")
    Optional<Reservation> lockByOrderId(@Param("orderId") Long orderId);
}

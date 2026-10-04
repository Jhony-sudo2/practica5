package com.sa.order.repositories;

import com.sa.order.models.SagaEvent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SagaEventRepository extends JpaRepository<SagaEvent, Long> {
    List<SagaEvent> findByOrderIdOrderByIdAsc(Long orderId);
}

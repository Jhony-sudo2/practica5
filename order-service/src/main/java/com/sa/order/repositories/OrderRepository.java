package com.sa.order.repositories;

import com.sa.order.models.*;

import org.springframework.data.jpa.repository.*;

import java.time.LocalDateTime;
import java.util.*;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "items")
    Optional<Order> findByRequestId(String requestId);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsById(Long id);

    List<Order> findByStatusIn(Collection<OrderStatus> states);

    List<Order> findByStatusAndUpdatedAtBefore(OrderStatus status, LocalDateTime before);
}

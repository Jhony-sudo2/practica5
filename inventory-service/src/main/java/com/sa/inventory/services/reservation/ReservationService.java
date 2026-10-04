package com.sa.inventory.services.reservation;

import com.sa.inventory.dto.InventoryDtos.*;

public interface ReservationService {
    ReservationView reserve(Reserve request);

    ReservationView release(Long orderId);

    ReservationView byOrder(Long orderId);
}

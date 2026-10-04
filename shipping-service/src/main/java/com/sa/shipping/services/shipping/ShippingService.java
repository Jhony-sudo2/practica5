package com.sa.shipping.services.shipping;

import com.sa.shipping.dto.ShippingRequest;
import com.sa.shipping.models.Shipment;

import java.util.*;

public interface ShippingService {
    Shipment schedule(ShippingRequest request);

    Shipment cancel(Long id);

    Shipment cancelOrder(Long orderId);

    Shipment get(Long id);

    Shipment byOrder(Long id);

    List<Shipment> all();
}

package com.sa.order.services.remoteservice;
import com.sa.order.dto.OrderDtos.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface RemoteService {
    ProductView product(Long id);
    PaymentView pay(Long id, BigDecimal total);
    ReservationView reserve(Long id, List<Item> items);
    ShipmentView schedule(Long id, String address);
    void refund(Long id);
    void release(Long id);
    void cancelShipment(Long id);
    Map<String, String> probe(String service);
}

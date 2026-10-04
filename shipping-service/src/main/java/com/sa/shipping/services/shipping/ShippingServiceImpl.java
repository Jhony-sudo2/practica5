package com.sa.shipping.services.shipping;

import com.sa.shipping.dto.ShippingRequest;
import com.sa.shipping.exceptions.BusinessException;
import com.sa.shipping.models.Shipment;
import com.sa.shipping.repositories.ShipmentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {
    private final ShipmentRepository repository;

    @Transactional
    public Shipment schedule(ShippingRequest request) {
        Shipment existing = repository.lockByOrderId(request.orderId()).orElse(null);
        if (existing != null) {
            if (!existing.getStatus().equals("SCHEDULED"))
                throw new BusinessException(
                        409, "Envío ya cancelado; se rechaza programación tardía");
            if (!Objects.equals(existing.getAddress(), request.address()))
                throw new BusinessException(409, "Dirección diferente para la misma orden");
            return existing;
        }
        Shipment s = new Shipment();
        s.setOrderId(request.orderId());
        s.setAddress(request.address());
        s.setStatus("SCHEDULED");
        s.setScheduledDate(LocalDateTime.now().plusDays(2));
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(s.getCreatedAt());
        return repository.saveAndFlush(s);
    }

    @Transactional
    public Shipment cancel(Long id) {
        return cancelOrder(get(id).getOrderId());
    }

    @Transactional
    public Shipment cancelOrder(Long id) {
        Shipment s =
                repository
                        .lockByOrderId(id)
                        .orElseGet(
                                () -> {
                                    Shipment n = new Shipment();
                                    n.setOrderId(id);
                                    n.setCreatedAt(LocalDateTime.now());
                                    return n;
                                });
        s.setStatus("CANCELLED");
        s.setUpdatedAt(LocalDateTime.now());
        return repository.saveAndFlush(s);
    }

    public Shipment get(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new BusinessException(404, "Envío no encontrado"));
    }

    public Shipment byOrder(Long id) {
        return repository
                .findByOrderId(id)
                .orElseThrow(() -> new BusinessException(404, "Envío no encontrado"));
    }

    public List<Shipment> all() {
        return repository.findAll();
    }
}

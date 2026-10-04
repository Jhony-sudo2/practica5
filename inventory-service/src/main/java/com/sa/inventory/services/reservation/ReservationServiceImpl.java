package com.sa.inventory.services.reservation;

import com.sa.inventory.dto.InventoryDtos.*;
import com.sa.inventory.exceptions.BusinessException;
import com.sa.inventory.models.*;
import com.sa.inventory.repositories.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final ProductRepository products;
    private final ReservationRepository reservations;

    @Transactional
    public ReservationView reserve(Reserve request) {
        Map<Long, Integer> requested = new TreeMap<>();
        for (Item item : request.items())
            if (requested.put(item.productId(), item.quantity()) != null)
                throw new BusinessException(400, "Producto duplicado en el carrito");
        Reservation previous = reservations.lockByOrderId(request.orderId()).orElse(null);
        if (previous != null) {
            if (!previous.getStatus().equals("RESERVED"))
                throw new BusinessException(
                        409, "Reserva ya liberada; se rechaza una reserva tardía");
            Map<Long, Integer> saved = new TreeMap<>();
            previous.getItems().forEach(i -> saved.put(i.getProductId(), i.getQuantity()));
            if (!saved.equals(requested))
                throw new BusinessException(409, "Contenido diferente para la misma orden");
            return view(previous);
        }
        // Orden fijo de locks evita invertir el orden de bloqueo de productos concurrentes.
        Map<Long, Product> locked = new TreeMap<>();
        requested.forEach(
                (id, qty) -> {
                    Product p =
                            products.lockById(id)
                                    .orElseThrow(
                                            () ->
                                                    new BusinessException(
                                                            404, "Producto no encontrado: " + id));
                    if (p.getStock() < qty)
                        throw new BusinessException(409, "Sin stock para el producto " + id);
                    locked.put(id, p);
                });
        Reservation r = new Reservation();
        r.setOrderId(request.orderId());
        r.setStatus("RESERVED");
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(r.getCreatedAt());
        requested.forEach(
                (id, qty) -> {
                    Product p = locked.get(id);
                    p.setStock(p.getStock() - qty);
                    ReservationItem i = new ReservationItem();
                    i.setReservation(r);
                    i.setProductId(id);
                    i.setQuantity(qty);
                    r.getItems().add(i);
                });
        return view(reservations.saveAndFlush(r));
    }

    @Transactional
    public ReservationView release(Long orderId) {
        Reservation r = reservations.lockByOrderId(orderId).orElse(null);
        if (r == null) {
            r = new Reservation();
            r.setOrderId(orderId);
            r.setStatus("RELEASED");
            r.setCreatedAt(LocalDateTime.now());
            r.setUpdatedAt(r.getCreatedAt());
            return view(reservations.saveAndFlush(r));
        }
        if (r.getStatus().equals("RELEASED")) return view(r);
        r.getItems().stream()
                .sorted(Comparator.comparing(ReservationItem::getProductId))
                .forEach(
                        i -> {
                            Product p =
                                    products.lockById(i.getProductId())
                                            .orElseThrow(
                                                    () ->
                                                            new BusinessException(
                                                                    404, "Producto no encontrado"));
                            p.setStock(p.getStock() + i.getQuantity());
                        });
        r.setStatus("RELEASED");
        r.setUpdatedAt(LocalDateTime.now());
        return view(reservations.saveAndFlush(r));
    }

    @Transactional(readOnly = true)
    public ReservationView byOrder(Long id) {
        return view(
                reservations
                        .findByOrderId(id)
                        .orElseThrow(() -> new BusinessException(404, "Reserva no encontrada")));
    }

    private ReservationView view(Reservation r) {
        return new ReservationView(
                r.getId(),
                r.getOrderId(),
                r.getStatus(),
                r.getItems().stream()
                        .map(i -> new Item(i.getProductId(), i.getQuantity()))
                        .toList());
    }
}

package org.enterprise.sales.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.sales.entity.DeliveryNote;
import org.enterprise.sales.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryNote> create(@RequestBody DeliveryNote deliveryNote) {
        return ResponseEntity.ok(deliveryService.save(deliveryNote));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<DeliveryNote> confirmDelivery(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.confirmDelivery(id));
    }

    @PostMapping("/{id}/create-return")
    public ResponseEntity<DeliveryNote> createReturn(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.createReturn(id));
    }
}

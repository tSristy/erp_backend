package org.enterprise.crm.service.controller;

import org.enterprise.crm.service.entity.ServiceOrder;
import org.enterprise.crm.service.service.ServiceOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crm/service/service-orders")
public class ServiceOrderController {

    private final ServiceOrderService service;

    public ServiceOrderController(ServiceOrderService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ServiceOrder>> getAll(@RequestParam(required = false) Long serviceRequestId) {
        if (serviceRequestId != null) {
            return ResponseEntity.ok(service.findByServiceRequestId(serviceRequestId));
        }
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrder> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ServiceOrder> create(@RequestBody ServiceOrder order) {
        return ResponseEntity.ok(service.save(order));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceOrder> update(@PathVariable Long id, @RequestBody ServiceOrder order) {
        return service.findById(id).map(existing -> {
            order.setId(existing.getId());
            return ResponseEntity.ok(service.save(order));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

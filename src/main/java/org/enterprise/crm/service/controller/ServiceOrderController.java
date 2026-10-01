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

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<ServiceOrder> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long companyId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.searchServiceOrders(companyId, q, pageable);
    }

}
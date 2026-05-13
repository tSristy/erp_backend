package org.enterprise.crm.service.controller;

import org.enterprise.crm.service.entity.ServiceEstimate;
import org.enterprise.crm.service.service.ServiceEstimateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crm/service/estimates")
public class ServiceEstimateController {

    private final ServiceEstimateService service;

    public ServiceEstimateController(ServiceEstimateService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ServiceEstimate>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceEstimate> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ServiceEstimate> create(@RequestBody ServiceEstimate estimate) {
        if (estimate.getDetails() != null) {
            estimate.getDetails().forEach(d -> d.setServiceEstimate(estimate));
        }
        return ResponseEntity.ok(service.save(estimate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceEstimate> update(@PathVariable Long id, @RequestBody ServiceEstimate estimate) {
        return service.findById(id).map(existing -> {
            estimate.setId(existing.getId());
            if (estimate.getDetails() != null) {
                estimate.getDetails().forEach(d -> d.setServiceEstimate(estimate));
            }
            return ResponseEntity.ok(service.save(estimate));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

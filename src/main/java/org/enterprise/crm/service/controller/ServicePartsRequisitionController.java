package org.enterprise.crm.service.controller;

import org.enterprise.crm.service.entity.ServicePartsRequisition;
import org.enterprise.crm.service.service.ServicePartsRequisitionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crm/service/parts-requisitions")
public class ServicePartsRequisitionController {

    private final ServicePartsRequisitionService service;

    public ServicePartsRequisitionController(ServicePartsRequisitionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ServicePartsRequisition>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicePartsRequisition> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ServicePartsRequisition> create(@RequestBody ServicePartsRequisition requisition) {
        if (requisition.getDetails() != null) {
            requisition.getDetails().forEach(d -> d.setRequisition(requisition));
        }
        return ResponseEntity.ok(service.save(requisition));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicePartsRequisition> update(@PathVariable Long id, @RequestBody ServicePartsRequisition requisition) {
        return service.findById(id).map(existing -> {
            requisition.setId(existing.getId());
            if (requisition.getDetails() != null) {
                requisition.getDetails().forEach(d -> d.setRequisition(requisition));
            }
            return ResponseEntity.ok(service.save(requisition));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

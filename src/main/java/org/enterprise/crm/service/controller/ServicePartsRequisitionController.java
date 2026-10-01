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

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<ServicePartsRequisition> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long companyId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.searchServicePartsRequisitions(companyId, q, pageable);
    }

}
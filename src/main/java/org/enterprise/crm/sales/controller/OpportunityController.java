package org.enterprise.crm.sales.controller;

import org.enterprise.crm.sales.entity.Opportunity;
import org.enterprise.crm.sales.service.OpportunityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crm/sales/opportunities")
public class OpportunityController {

    private final OpportunityService service;

    public OpportunityController(OpportunityService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Opportunity>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Opportunity> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Opportunity> create(@RequestBody Opportunity opportunity) {
        return ResponseEntity.ok(service.save(opportunity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Opportunity> update(@PathVariable Long id, @RequestBody Opportunity opportunity) {
        return service.findById(id).map(existing -> {
            opportunity.setId(existing.getId());
            return ResponseEntity.ok(service.save(opportunity));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

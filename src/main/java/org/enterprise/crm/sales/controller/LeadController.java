package org.enterprise.crm.sales.controller;

import org.enterprise.crm.sales.entity.Lead;
import org.enterprise.crm.sales.service.LeadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crm/sales/leads")
public class LeadController {

    private final LeadService service;

    public LeadController(LeadService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Lead>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lead> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Lead> create(@RequestBody Lead lead) {
        return ResponseEntity.ok(service.save(lead));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lead> update(@PathVariable Long id, @RequestBody Lead lead) {
        return service.findById(id).map(existing -> {
            lead.setId(existing.getId());
            return ResponseEntity.ok(service.save(lead));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

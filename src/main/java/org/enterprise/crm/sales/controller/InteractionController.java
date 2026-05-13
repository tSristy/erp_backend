package org.enterprise.crm.sales.controller;

import org.enterprise.crm.sales.entity.Interaction;
import org.enterprise.crm.sales.service.InteractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crm/sales/interactions")
public class InteractionController {

    private final InteractionService service;

    public InteractionController(InteractionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Interaction>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Interaction> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Interaction> create(@RequestBody Interaction interaction) {
        return ResponseEntity.ok(service.save(interaction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Interaction> update(@PathVariable Long id, @RequestBody Interaction interaction) {
        return service.findById(id).map(existing -> {
            interaction.setId(existing.getId());
            return ResponseEntity.ok(service.save(interaction));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

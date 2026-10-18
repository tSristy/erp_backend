package org.enterprise.crm.service.controller;

import org.enterprise.crm.service.entity.TechnicianStockMovement;
import org.enterprise.crm.service.service.TechnicianStockMovementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crm/service/technician-stock-movements")
public class TechnicianStockMovementController {

    private final TechnicianStockMovementService service;

    public TechnicianStockMovementController(TechnicianStockMovementService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TechnicianStockMovement>> getAll() {
        return ResponseEntity.ok(service.findAllByCurrentCompany());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TechnicianStockMovement> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TechnicianStockMovement> create(@RequestBody TechnicianStockMovement movement) {
        movement.setId(null);
        return ResponseEntity.ok(service.save(movement));
    }
}

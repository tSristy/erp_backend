package org.enterprise.production.controller;

import org.enterprise.production.dto.ManufacturingOrderDTO;
import org.enterprise.production.service.ManufacturingOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/production/manufacturing-orders")
public class ManufacturingOrderController {

    private final ManufacturingOrderService service;

    public ManufacturingOrderController(ManufacturingOrderService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ManufacturingOrderDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManufacturingOrderDTO> getById(@PathVariable Long id) {
        ManufacturingOrderDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ManufacturingOrderDTO> create(@RequestBody ManufacturingOrderDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ManufacturingOrderDTO> update(@PathVariable Long id, @RequestBody ManufacturingOrderDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

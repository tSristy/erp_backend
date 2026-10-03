package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.StockReclassification;
import org.enterprise.inventory.service.StockReclassificationService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/inventory/stock-reclassifications")
@RequiredArgsConstructor
public class StockReclassificationController {

    private final StockReclassificationService stockReclassificationService;

    @PostMapping
    public ResponseEntity<StockReclassification> create(@Valid @RequestBody StockReclassification stockReclassification) {
        return ResponseEntity.ok(stockReclassificationService.save(stockReclassification));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<StockReclassification> completeReclassification(@PathVariable Long id) {
        return ResponseEntity.ok(stockReclassificationService.completeReclassification(id));
    }

    @GetMapping
    public ResponseEntity<java.util.List<StockReclassification>> getAll() {
        return ResponseEntity.ok(stockReclassificationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockReclassification> getById(@PathVariable Long id) {
        return stockReclassificationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockReclassification> update(@PathVariable Long id, @Valid @RequestBody StockReclassification stockReclassification) {
        stockReclassification.setId(id);
        return ResponseEntity.ok(stockReclassificationService.save(stockReclassification));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stockReclassificationService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.StockReclassification> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return stockReclassificationService.searchStockReclassifications(q, pageable);
    }
}

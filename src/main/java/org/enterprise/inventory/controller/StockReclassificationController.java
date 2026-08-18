package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.StockReclassification;
import org.enterprise.inventory.service.StockReclassificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-reclassifications")
@RequiredArgsConstructor
public class StockReclassificationController {

    private final StockReclassificationService stockReclassificationService;

    @PostMapping
    public ResponseEntity<StockReclassification> create(@RequestBody StockReclassification stockReclassification) {
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
    public ResponseEntity<StockReclassification> update(@PathVariable Long id, @RequestBody StockReclassification stockReclassification) {
        stockReclassification.setId(id);
        return ResponseEntity.ok(stockReclassificationService.save(stockReclassification));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stockReclassificationService.delete(id);
        return ResponseEntity.ok().build();
    }
}

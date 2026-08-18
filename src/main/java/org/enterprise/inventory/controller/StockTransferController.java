package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.StockTransfer;
import org.enterprise.inventory.service.StockTransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-transfers")
@RequiredArgsConstructor
public class StockTransferController {

    private final StockTransferService stockTransferService;

    @PostMapping
    public ResponseEntity<StockTransfer> create(@RequestBody StockTransfer stockTransfer) {
        return ResponseEntity.ok(stockTransferService.save(stockTransfer));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<StockTransfer> completeTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(stockTransferService.completeTransfer(id));
    }

    @GetMapping
    public ResponseEntity<java.util.List<StockTransfer>> getAll() {
        return ResponseEntity.ok(stockTransferService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockTransfer> getById(@PathVariable Long id) {
        return stockTransferService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockTransfer> update(@PathVariable Long id, @RequestBody StockTransfer stockTransfer) {
        stockTransfer.setId(id);
        return ResponseEntity.ok(stockTransferService.save(stockTransfer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stockTransferService.delete(id);
        return ResponseEntity.ok().build();
    }
}

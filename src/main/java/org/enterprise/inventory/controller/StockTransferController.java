package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.StockTransfer;
import org.enterprise.inventory.service.StockTransferService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/inventory/stock-transfers")
@RequiredArgsConstructor
public class StockTransferController {

    private final StockTransferService stockTransferService;

    @PostMapping
    public ResponseEntity<StockTransfer> create(@Valid @RequestBody StockTransfer stockTransfer) {
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
    public ResponseEntity<StockTransfer> update(@PathVariable Long id, @Valid @RequestBody StockTransfer stockTransfer) {
        stockTransfer.setId(id);
        return ResponseEntity.ok(stockTransferService.save(stockTransfer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stockTransferService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.StockTransfer> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return stockTransferService.searchStockTransfers(q, pageable);
    }
}

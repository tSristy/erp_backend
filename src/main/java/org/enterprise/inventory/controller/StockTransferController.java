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
}

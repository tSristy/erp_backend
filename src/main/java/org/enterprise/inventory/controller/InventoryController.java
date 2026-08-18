package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.dto.InventoryTransactionRequest;
import org.enterprise.inventory.dto.StockBalanceResponse;
import org.enterprise.inventory.entity.Inventory;
import org.enterprise.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public List<Inventory> getByProduct(@PathVariable Long productId) {
        return service.getByProduct(productId);
    }

    @PostMapping("/adjust/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public void adjustStock(@PathVariable Long id,
                            @RequestParam BigDecimal qty) {
        service.adjustStock(id, qty);
    }

    @PostMapping("/receive")
    @PreAuthorize("hasAuthority('INVENTORY_RECEIVE')")
    public ResponseEntity<?> receiveStock(
            @RequestBody InventoryTransactionRequest request
    ) {
        service.receiveStock(request);
        return ResponseEntity.ok("Stock received successfully");
    }

    @PostMapping("/issue")
    @PreAuthorize("hasAuthority('INVENTORY_ISSUE')")
    public ResponseEntity<?> issueStock(
            @RequestBody InventoryTransactionRequest request
    ) {
        service.issueStock(request);
        return ResponseEntity.ok("Stock issued successfully");
    }

    @GetMapping("/stock-balance")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<StockBalanceResponse> getStockBalance(
            @RequestParam Long itemId,
            @RequestParam Long warehouseId,
            @RequestParam(required = false) Long locationId
    ) {
        return ResponseEntity.ok(
                service.getStockBalance(
                        itemId,
                        warehouseId,
                        locationId
                )
        );
    }
}
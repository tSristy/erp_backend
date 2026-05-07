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
    public List<Inventory> getByProduct(@PathVariable Long productId,
                                        @RequestParam Long companyId) {
        return service.getByProduct(productId, companyId);
    }

    @PostMapping("/adjust/{id}")
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

    @GetMapping("/stock-balance")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<StockBalanceResponse> getStockBalance(
            @RequestParam Long itemId,
            @RequestParam Long warehouseId,
            @RequestParam Long locationId
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
package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.dto.InventoryTransactionRequest;
import org.enterprise.inventory.dto.StockBalanceResponse;
import org.enterprise.inventory.entity.Inventory;
import org.enterprise.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
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


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.Inventory> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.searchInventorys(q, pageable);
    }
}
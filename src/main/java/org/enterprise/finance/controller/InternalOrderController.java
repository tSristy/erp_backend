package org.enterprise.finance.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.entity.InternalOrder;
import org.enterprise.finance.service.InternalOrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/internal-orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final InternalOrderService internalOrderService;

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCE_READ')")
    public ResponseEntity<Page<InternalOrder>> getInternalOrders(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        return ResponseEntity.ok(internalOrderService.searchInternalOrders(search, page > 0 ? page - 1 : 0, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCE_READ')")
    public ResponseEntity<InternalOrder> getInternalOrder(@PathVariable Long id) {
        return ResponseEntity.ok(internalOrderService.getInternalOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FINANCE_WRITE')")
    public ResponseEntity<InternalOrder> createInternalOrder(@RequestBody InternalOrder internalOrder) {
        return ResponseEntity.ok(internalOrderService.createInternalOrder(internalOrder));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCE_WRITE')")
    public ResponseEntity<InternalOrder> updateInternalOrder(@PathVariable Long id, @RequestBody InternalOrder internalOrder) {
        return ResponseEntity.ok(internalOrderService.updateInternalOrder(id, internalOrder));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCE_WRITE')")
    public ResponseEntity<Void> deleteInternalOrder(@PathVariable Long id) {
        internalOrderService.deleteInternalOrder(id);
        return ResponseEntity.ok().build();
    }
}

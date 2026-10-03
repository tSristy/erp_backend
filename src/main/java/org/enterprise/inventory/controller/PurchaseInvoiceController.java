package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.PurchaseInvoice;
import org.enterprise.inventory.service.PurchaseInvoiceService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/purchase-invoices")
@RequiredArgsConstructor
public class PurchaseInvoiceController {

    private final PurchaseInvoiceService purchaseInvoiceService;

    @GetMapping
    public ResponseEntity<List<PurchaseInvoice>> getAllInvoices() {
        return ResponseEntity.ok(purchaseInvoiceService.getAllInvoices());
    }

    @GetMapping("/unpaid")
    public ResponseEntity<List<PurchaseInvoice>> getUnpaidInvoices(@RequestParam Long vendorId) {
        return ResponseEntity.ok(purchaseInvoiceService.getUnpaidInvoicesByVendor(vendorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseInvoice> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseInvoiceService.getInvoiceById(id));
    }

    @PostMapping
    public ResponseEntity<PurchaseInvoice> createInvoice(@Valid @RequestBody PurchaseInvoice invoice) {
        return ResponseEntity.ok(purchaseInvoiceService.createInvoice(invoice));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PurchaseInvoice> updateStatus(@PathVariable Long id, @RequestParam PurchaseInvoice.InvoiceStatus status) {
        return ResponseEntity.ok(purchaseInvoiceService.updateInvoiceStatus(id, status));
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.PurchaseInvoice> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return purchaseInvoiceService.searchPurchaseInvoices(q, pageable);
    }
}

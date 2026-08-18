package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.PurchaseInvoice;
import org.enterprise.inventory.service.PurchaseInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/purchase-invoices")
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
    public ResponseEntity<PurchaseInvoice> createInvoice(@RequestBody PurchaseInvoice invoice) {
        return ResponseEntity.ok(purchaseInvoiceService.createInvoice(invoice));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PurchaseInvoice> updateStatus(@PathVariable Long id, @RequestParam PurchaseInvoice.InvoiceStatus status) {
        return ResponseEntity.ok(purchaseInvoiceService.updateInvoiceStatus(id, status));
    }
}

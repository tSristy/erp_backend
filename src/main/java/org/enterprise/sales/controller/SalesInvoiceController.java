package org.enterprise.sales.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.sales.entity.SalesInvoice;
import org.enterprise.sales.service.SalesInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-invoices")
@RequiredArgsConstructor
public class SalesInvoiceController {

    private final SalesInvoiceService salesInvoiceService;

    @PostMapping
    public ResponseEntity<SalesInvoice> create(@RequestBody SalesInvoice salesInvoice) {
        return ResponseEntity.ok(salesInvoiceService.save(salesInvoice));
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<SalesInvoice> postInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(salesInvoiceService.postInvoice(id));
    }

    @PostMapping("/{id}/create-credit-memo")
    public ResponseEntity<SalesInvoice> createCreditMemo(@PathVariable Long id) {
        return ResponseEntity.ok(salesInvoiceService.createCreditMemo(id));
    }
}

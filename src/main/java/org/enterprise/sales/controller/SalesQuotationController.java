package org.enterprise.sales.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.sales.entity.SalesQuotation;
import org.enterprise.sales.service.SalesQuotationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-quotations")
@RequiredArgsConstructor
public class SalesQuotationController {

    private final SalesQuotationService salesQuotationService;

    @PostMapping
    public ResponseEntity<SalesQuotation> create(@RequestBody SalesQuotation salesQuotation) {
        return ResponseEntity.ok(salesQuotationService.save(salesQuotation));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SalesQuotation> updateStatus(
            @PathVariable Long id,
            @RequestParam SalesQuotation.QuotationStatus status) {
        return ResponseEntity.ok(salesQuotationService.updateStatus(id, status));
    }
}

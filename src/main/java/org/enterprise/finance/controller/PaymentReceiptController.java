package org.enterprise.finance.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.entity.PaymentReceipt;
import org.enterprise.finance.service.PaymentReceiptService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/payment-receipts")
@RequiredArgsConstructor
public class PaymentReceiptController {

    private final PaymentReceiptService paymentReceiptService;

    @GetMapping
    public ResponseEntity<List<PaymentReceipt>> getAllReceipts() {
        return ResponseEntity.ok(paymentReceiptService.getAllReceipts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentReceipt> getReceipt(@PathVariable Long id) {
        return ResponseEntity.ok(paymentReceiptService.getReceiptById(id));
    }

    @PostMapping
    public ResponseEntity<PaymentReceipt> createReceipt(@Valid @RequestBody PaymentReceipt receipt) {
        return ResponseEntity.ok(paymentReceiptService.createReceipt(receipt));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentReceipt> updateStatus(@PathVariable Long id, @RequestParam PaymentReceipt.PaymentStatus status) {
        return ResponseEntity.ok(paymentReceiptService.updateStatus(id, status));
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<PaymentReceipt> updateReceipt(@PathVariable Long id, @Valid @RequestBody PaymentReceipt receipt) {
        return ResponseEntity.ok(paymentReceiptService.updateReceipt(id, receipt));
    }

    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deleteReceipt(@PathVariable Long id) {
        paymentReceiptService.deleteReceipt(id);
        return ResponseEntity.noContent().build();
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.PaymentReceipt> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return paymentReceiptService.searchPaymentReceipts(q, pageable);
    }
}

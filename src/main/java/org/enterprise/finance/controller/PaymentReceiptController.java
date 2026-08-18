package org.enterprise.finance.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.entity.PaymentReceipt;
import org.enterprise.finance.service.PaymentReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/payment-receipts")
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
    public ResponseEntity<PaymentReceipt> createReceipt(@RequestBody PaymentReceipt receipt) {
        return ResponseEntity.ok(paymentReceiptService.createReceipt(receipt));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentReceipt> updateStatus(@PathVariable Long id, @RequestParam PaymentReceipt.PaymentStatus status) {
        return ResponseEntity.ok(paymentReceiptService.updateStatus(id, status));
    }
}

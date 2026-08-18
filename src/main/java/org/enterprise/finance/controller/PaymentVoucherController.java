package org.enterprise.finance.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.entity.PaymentVoucher;
import org.enterprise.finance.service.PaymentVoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/payment-vouchers")
@RequiredArgsConstructor
public class PaymentVoucherController {

    private final PaymentVoucherService paymentVoucherService;

    @GetMapping
    public ResponseEntity<List<PaymentVoucher>> getAllVouchers() {
        return ResponseEntity.ok(paymentVoucherService.getAllVouchers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentVoucher> getVoucher(@PathVariable Long id) {
        return ResponseEntity.ok(paymentVoucherService.getVoucherById(id));
    }

    @PostMapping
    public ResponseEntity<PaymentVoucher> createVoucher(@RequestBody PaymentVoucher voucher) {
        return ResponseEntity.ok(paymentVoucherService.createVoucher(voucher));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentVoucher> updateStatus(@PathVariable Long id, @RequestParam PaymentVoucher.PaymentStatus status) {
        return ResponseEntity.ok(paymentVoucherService.updateStatus(id, status));
    }
}

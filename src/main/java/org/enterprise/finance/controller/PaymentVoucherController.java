package org.enterprise.finance.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.entity.PaymentVoucher;
import org.enterprise.finance.service.PaymentVoucherService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/payment-vouchers")
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
    public ResponseEntity<PaymentVoucher> createVoucher(@Valid @RequestBody PaymentVoucher voucher) {
        return ResponseEntity.ok(paymentVoucherService.createVoucher(voucher));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentVoucher> updateStatus(@PathVariable Long id, @RequestParam PaymentVoucher.PaymentStatus status) {
        return ResponseEntity.ok(paymentVoucherService.updateStatus(id, status));
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<PaymentVoucher> updateVoucher(@PathVariable Long id, @Valid @RequestBody PaymentVoucher voucher) {
        return ResponseEntity.ok(paymentVoucherService.updateVoucher(id, voucher));
    }

    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        paymentVoucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.PaymentVoucher> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return paymentVoucherService.searchPaymentVouchers(q, pageable);
    }
}

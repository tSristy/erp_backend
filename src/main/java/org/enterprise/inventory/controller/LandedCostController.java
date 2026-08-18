package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.LandedCostVoucher;
import org.enterprise.inventory.service.LandedCostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/landed-cost-vouchers")
@RequiredArgsConstructor
public class LandedCostController {

    private final LandedCostService landedCostService;

    @GetMapping
    public ResponseEntity<List<LandedCostVoucher>> getAllVouchers() {
        return ResponseEntity.ok(landedCostService.getAllVouchers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LandedCostVoucher> getVoucher(@PathVariable Long id) {
        return ResponseEntity.ok(landedCostService.getVoucherById(id));
    }

    @PostMapping
    public ResponseEntity<LandedCostVoucher> createVoucher(@RequestBody LandedCostVoucher voucher) {
        return ResponseEntity.ok(landedCostService.createVoucher(voucher));
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<LandedCostVoucher> postVoucher(@PathVariable Long id) {
        return ResponseEntity.ok(landedCostService.postVoucher(id));
    }
}

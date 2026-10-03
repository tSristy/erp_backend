package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.LandedCostVoucher;
import org.enterprise.inventory.service.LandedCostService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/landed-cost-vouchers")
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
    public ResponseEntity<LandedCostVoucher> createVoucher(@Valid @RequestBody LandedCostVoucher voucher) {
        return ResponseEntity.ok(landedCostService.createVoucher(voucher));
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<LandedCostVoucher> postVoucher(@PathVariable Long id) {
        return ResponseEntity.ok(landedCostService.postVoucher(id));
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.LandedCostVoucher> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return landedCostService.searchLandedCostVouchers(q, pageable);
    }
}

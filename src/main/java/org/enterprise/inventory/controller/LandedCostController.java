package org.enterprise.inventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.LandedCostVoucher;
import org.enterprise.inventory.service.LandedCostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/{id}")
    public ResponseEntity<LandedCostVoucher> updateVoucher(@PathVariable Long id, @Valid @RequestBody LandedCostVoucher voucher) {
        return ResponseEntity.ok(landedCostService.updateVoucher(id, voucher));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        landedCostService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<LandedCostVoucher> postVoucher(@PathVariable Long id) {
        return ResponseEntity.ok(landedCostService.postVoucher(id));
    }

    @GetMapping("/search")
    public Page<LandedCostVoucher> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        String searchQuery = (query != null && !query.trim().isEmpty()) ? query : q;
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return landedCostService.searchLandedCostVouchers(searchQuery, pageable);
    }
}

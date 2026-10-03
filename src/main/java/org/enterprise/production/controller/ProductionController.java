package org.enterprise.production.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.production.entity.ManufacturingOrder;
import org.enterprise.production.service.ProductionService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/production/production-orders")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService productionService;

    @PostMapping("/{id}/complete")
    public ResponseEntity<String> completeProduction(
            @PathVariable Long id,
            @RequestParam BigDecimal producedQty) {
        
        productionService.completeProduction(id, producedQty);
        return ResponseEntity.ok("Production completed successfully for quantity: " + producedQty);
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<ManufacturingOrder> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return productionService.searchProductions(org.enterprise.common.util.TenantContext.getCompanyId(), q, pageable);
    }

}
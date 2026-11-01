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
@RequestMapping({"/api/v1/production/production-orders", "/api/v1/production-orders"})
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService productionService;

    @PostMapping("/{id}/complete")
    public ResponseEntity<String> completeProduction(
            @PathVariable Long id,
            @RequestParam BigDecimal producedQty,
            @RequestParam(required = false) BigDecimal rejectedQty,
            @RequestParam(required = false) Long qcInspectorId,
            @RequestParam(required = false) String qcNotes,
            @RequestParam(required = false) java.util.List<String> serialNumbers,
            @RequestParam(required = false) String batchNo,
            @RequestBody(required = false) java.util.Map<String, Object> body) {

        java.util.List<String> resolvedSerials = serialNumbers;
        if ((resolvedSerials == null || resolvedSerials.isEmpty()) && body != null && body.get("serialNumbers") instanceof java.util.List<?> rawList) {
            resolvedSerials = rawList.stream()
                    .filter(java.util.Objects::nonNull)
                    .map(Object::toString)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        String resolvedBatchNo = batchNo;
        if ((resolvedBatchNo == null || resolvedBatchNo.isBlank()) && body != null && body.get("batchNo") != null) {
            resolvedBatchNo = body.get("batchNo").toString().trim();
        }

        productionService.completeProduction(id, producedQty, rejectedQty, qcInspectorId, qcNotes, resolvedSerials, resolvedBatchNo);
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
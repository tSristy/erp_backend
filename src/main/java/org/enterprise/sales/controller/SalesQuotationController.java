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
    private final org.enterprise.sales.mapper.SalesMapper mapper;

    @PostMapping
    public ResponseEntity<org.enterprise.sales.dto.SalesQuotationDto> create(@RequestBody org.enterprise.sales.dto.SalesQuotationDto dto) {
        SalesQuotation entity = mapper.toEntity(dto);
        SalesQuotation saved = salesQuotationService.save(entity);
        return ResponseEntity.ok(mapper.toDto(saved));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<org.enterprise.sales.dto.SalesQuotationDto> updateStatus(
            @PathVariable Long id,
            @RequestParam SalesQuotation.QuotationStatus status) {
        return ResponseEntity.ok(mapper.toDto(salesQuotationService.updateStatus(id, status)));
    }

    @GetMapping
    public ResponseEntity<java.util.List<org.enterprise.sales.dto.SalesQuotationDto>> getAll() {
        return ResponseEntity.ok(mapper.toDtoListSalesQuotation(salesQuotationService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<org.enterprise.sales.dto.SalesQuotationDto> getById(@PathVariable Long id) {
        return salesQuotationService.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<org.enterprise.sales.dto.SalesQuotationDto> update(@PathVariable Long id, @RequestBody org.enterprise.sales.dto.SalesQuotationDto dto) {
        dto.setId(id);
        SalesQuotation entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(salesQuotationService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        salesQuotationService.delete(id);
        return ResponseEntity.ok().build();
    }
}

package org.enterprise.sales.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.sales.entity.SalesQuotation;
import org.enterprise.sales.service.SalesQuotationService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/sales/quotations")
@RequiredArgsConstructor
public class SalesQuotationController {

    private final SalesQuotationService salesQuotationService;
    private final org.enterprise.sales.mapper.SalesMapper mapper;

    @GetMapping("/next-no")
    public ResponseEntity<java.util.Map<String, String>> getNextQuotationNo() {
        String nextNo = salesQuotationService.generateNextQuotationNo(org.enterprise.common.util.TenantContext.getCompanyId());
        return ResponseEntity.ok(java.util.Collections.singletonMap("quotationNo", nextNo));
    }

    @PostMapping
    public ResponseEntity<org.enterprise.sales.dto.SalesQuotationDto> create(@Valid @RequestBody org.enterprise.sales.dto.SalesQuotationDto dto) {
        SalesQuotation entity = mapper.toEntity(dto);
        SalesQuotation saved = salesQuotationService.save(entity);
        org.enterprise.sales.dto.SalesQuotationDto created = mapper.toDto(saved);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<org.enterprise.sales.dto.SalesQuotationDto> updateStatus(
            @PathVariable Long id,
            @RequestParam SalesQuotation.QuotationStatus status) {
        return ResponseEntity.ok(mapper.toDto(salesQuotationService.updateStatus(id, status)));
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String q,
            @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        String searchTerm = org.springframework.util.StringUtils.hasText(query) ? query : (org.springframework.util.StringUtils.hasText(search) ? search : q);
        if (org.springframework.util.StringUtils.hasText(searchTerm)) {
            return ResponseEntity.ok(salesQuotationService.search(searchTerm, pageable).map(mapper::toDto));
        }
        return ResponseEntity.ok(salesQuotationService.findAll(pageable).map(mapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<org.enterprise.sales.dto.SalesQuotationDto> getById(@PathVariable Long id) {
        return salesQuotationService.findDtoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<org.enterprise.sales.dto.SalesQuotationDto> update(@PathVariable Long id, @Valid @RequestBody org.enterprise.sales.dto.SalesQuotationDto dto) {
        dto.setId(id);
        SalesQuotation entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(salesQuotationService.update(id, entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        salesQuotationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<org.springframework.data.domain.Page<org.enterprise.sales.dto.SalesQuotationDto>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String q,
            @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        String searchTerm = org.springframework.util.StringUtils.hasText(query) ? query : (org.springframework.util.StringUtils.hasText(search) ? search : q);
        return ResponseEntity.ok(salesQuotationService.search(searchTerm, pageable).map(mapper::toDto));
    }
}
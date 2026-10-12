package org.enterprise.sales.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.sales.entity.SalesInvoice;
import org.enterprise.sales.service.SalesInvoiceService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/sales/invoices")
@RequiredArgsConstructor
public class SalesInvoiceController {

    private final SalesInvoiceService salesInvoiceService;
    private final org.enterprise.sales.mapper.SalesMapper mapper;

    @GetMapping("/next-no")
    public ResponseEntity<java.util.Map<String, String>> getNextInvoiceNo() {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        return ResponseEntity.ok(java.util.Map.of("invoiceNo", salesInvoiceService.generateNextInvoiceNo(companyId)));
    }

    @PostMapping
    public ResponseEntity<org.enterprise.sales.dto.SalesInvoiceDto> create(@Valid @RequestBody org.enterprise.sales.dto.SalesInvoiceDto dto) {
        SalesInvoice entity = mapper.toEntity(dto);
        SalesInvoice saved = salesInvoiceService.save(entity);
        org.enterprise.sales.dto.SalesInvoiceDto created = mapper.toDto(saved);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<org.enterprise.sales.dto.SalesInvoiceDto> postInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(salesInvoiceService.postInvoice(id)));
    }

    @PostMapping("/{id}/create-credit-memo")
    public ResponseEntity<org.enterprise.sales.dto.SalesInvoiceDto> createCreditMemo(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(salesInvoiceService.createCreditMemo(id)));
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String q,
            @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        String searchTerm = org.springframework.util.StringUtils.hasText(query) ? query : (org.springframework.util.StringUtils.hasText(search) ? search : q);
        if (org.springframework.util.StringUtils.hasText(searchTerm)) {
            return ResponseEntity.ok(salesInvoiceService.search(searchTerm, pageable).map(mapper::toDto));
        }
        return ResponseEntity.ok(salesInvoiceService.findAll(pageable).map(mapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<org.enterprise.sales.dto.SalesInvoiceDto> getById(@PathVariable Long id) {
        return salesInvoiceService.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<org.enterprise.sales.dto.SalesInvoiceDto> update(@PathVariable Long id, @Valid @RequestBody org.enterprise.sales.dto.SalesInvoiceDto dto) {
        dto.setId(id);
        SalesInvoice entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(salesInvoiceService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        salesInvoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public ResponseEntity<org.springframework.data.domain.Page<org.enterprise.sales.dto.SalesInvoiceDto>> search(
            @RequestParam(required = false) String query,
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(salesInvoiceService.search(query, pageable).map(mapper::toDto));
    }
}
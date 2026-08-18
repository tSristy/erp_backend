package org.enterprise.sales.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.sales.entity.SalesInvoice;
import org.enterprise.sales.service.SalesInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-invoices")
@RequiredArgsConstructor
public class SalesInvoiceController {

    private final SalesInvoiceService salesInvoiceService;
    private final org.enterprise.sales.mapper.SalesMapper mapper;

    @PostMapping
    public ResponseEntity<org.enterprise.sales.dto.SalesInvoiceDto> create(@RequestBody org.enterprise.sales.dto.SalesInvoiceDto dto) {
        SalesInvoice entity = mapper.toEntity(dto);
        SalesInvoice saved = salesInvoiceService.save(entity);
        return ResponseEntity.ok(mapper.toDto(saved));
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
    public ResponseEntity<java.util.List<org.enterprise.sales.dto.SalesInvoiceDto>> getAll() {
        return ResponseEntity.ok(mapper.toDtoListSalesInvoice(salesInvoiceService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<org.enterprise.sales.dto.SalesInvoiceDto> getById(@PathVariable Long id) {
        return salesInvoiceService.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<org.enterprise.sales.dto.SalesInvoiceDto> update(@PathVariable Long id, @RequestBody org.enterprise.sales.dto.SalesInvoiceDto dto) {
        dto.setId(id);
        SalesInvoice entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(salesInvoiceService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        salesInvoiceService.delete(id);
        return ResponseEntity.ok().build();
    }
}

package org.enterprise.sales.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.sales.entity.SalesOrder;
import org.enterprise.sales.service.SalesOrderService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/sales/orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;
    private final org.enterprise.sales.mapper.SalesMapper mapper;

    @GetMapping("/next-no")
    public ResponseEntity<java.util.Map<String, String>> getNextOrderNo() {
        String nextNo = salesOrderService.generateNextOrderNo(org.enterprise.common.util.TenantContext.getCompanyId());
        return ResponseEntity.ok(java.util.Collections.singletonMap("orderNo", nextNo));
    }

    @PostMapping
    public ResponseEntity<org.enterprise.sales.dto.SalesOrderDto> create(@Valid @RequestBody org.enterprise.sales.dto.SalesOrderDto dto) {
        SalesOrder entity = mapper.toEntity(dto);
        SalesOrder saved = salesOrderService.save(entity);
        org.enterprise.sales.dto.SalesOrderDto created = mapper.toDto(saved);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<org.enterprise.sales.dto.SalesOrderDto> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(salesOrderService.confirmOrder(id)));
    }

    @PostMapping("/{id}/create-return")
    public ResponseEntity<org.enterprise.sales.dto.SalesOrderDto> createReturn(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(salesOrderService.createReturn(id)));
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String q,
            @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        String searchTerm = org.springframework.util.StringUtils.hasText(query) ? query : (org.springframework.util.StringUtils.hasText(search) ? search : q);
        if (org.springframework.util.StringUtils.hasText(searchTerm)) {
            return ResponseEntity.ok(salesOrderService.search(searchTerm, pageable).map(mapper::toDto));
        }
        return ResponseEntity.ok(salesOrderService.findAll(pageable).map(mapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<org.enterprise.sales.dto.SalesOrderDto> getById(@PathVariable Long id) {
        return salesOrderService.findDtoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<org.enterprise.sales.dto.SalesOrderDto> update(@PathVariable Long id, @Valid @RequestBody org.enterprise.sales.dto.SalesOrderDto dto) {
        dto.setId(id);
        SalesOrder entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(salesOrderService.update(id, entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        salesOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public ResponseEntity<org.springframework.data.domain.Page<org.enterprise.sales.dto.SalesOrderDto>> search(
            @RequestParam(required = false) String query,
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(salesOrderService.search(query, pageable).map(mapper::toDto));
    }
}
package org.enterprise.sales.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.sales.entity.SalesOrder;
import org.enterprise.sales.service.SalesOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;
    private final org.enterprise.sales.mapper.SalesMapper mapper;

    @PostMapping
    public ResponseEntity<org.enterprise.sales.dto.SalesOrderDto> create(@RequestBody org.enterprise.sales.dto.SalesOrderDto dto) {
        SalesOrder entity = mapper.toEntity(dto);
        SalesOrder saved = salesOrderService.save(entity);
        return ResponseEntity.ok(mapper.toDto(saved));
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
    public ResponseEntity<java.util.List<org.enterprise.sales.dto.SalesOrderDto>> getAll() {
        return ResponseEntity.ok(mapper.toDtoListSalesOrder(salesOrderService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<org.enterprise.sales.dto.SalesOrderDto> getById(@PathVariable Long id) {
        return salesOrderService.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<org.enterprise.sales.dto.SalesOrderDto> update(@PathVariable Long id, @RequestBody org.enterprise.sales.dto.SalesOrderDto dto) {
        dto.setId(id);
        SalesOrder entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(salesOrderService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        salesOrderService.delete(id);
        return ResponseEntity.ok().build();
    }
}

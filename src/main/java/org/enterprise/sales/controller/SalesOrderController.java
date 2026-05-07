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

    @PostMapping
    public ResponseEntity<SalesOrder> create(@RequestBody SalesOrder salesOrder) {
        return ResponseEntity.ok(salesOrderService.save(salesOrder));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<SalesOrder> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.confirmOrder(id));
    }

    @PostMapping("/{id}/create-return")
    public ResponseEntity<SalesOrder> createReturn(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.createReturn(id));
    }
}

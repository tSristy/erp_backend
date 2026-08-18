package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping({"/api/inventory/reports", "/inventory/reports"})
@RequiredArgsConstructor
public class InventoryReportController {

    private final org.enterprise.inventory.service.InventoryReportService reportService;

    @GetMapping("/movement-register")
    public ResponseEntity<List<Object>> getMovementRegister(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        return ResponseEntity.ok(new java.util.ArrayList<>(reportService.getMovementRegister(companyId, warehouseId, locationId, startDate, endDate)));
    }

    @GetMapping("/stock")
    public ResponseEntity<List<Object>> getInventoryStock(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) LocalDate asOfDate) {
        
        return ResponseEntity.ok(new java.util.ArrayList<>(reportService.getInventoryStock(companyId, warehouseId, locationId, asOfDate)));
    }

    @GetMapping("/aging")
    public ResponseEntity<List<Object>> getInventoryAging(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) LocalDate asOfDate) {
        
        return ResponseEntity.ok(new java.util.ArrayList<>(reportService.getInventoryAging(companyId, warehouseId, locationId, asOfDate)));
    }

    @GetMapping("/valuation")
    public ResponseEntity<List<Object>> getInventoryValuation(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) LocalDate asOfDate) {
        
        return ResponseEntity.ok(new java.util.ArrayList<>(reportService.getInventoryValuation(companyId, warehouseId, locationId, asOfDate)));
    }

    @GetMapping("/current-serial-no")
    public ResponseEntity<List<Object>> getCurrentStockSerialNo(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long locationId) {
        
        return ResponseEntity.ok(new java.util.ArrayList<>(reportService.getCurrentStockSerialNo(companyId, warehouseId, locationId)));
    }

    @GetMapping("/purchase-summary")
    public ResponseEntity<List<Object>> getPurchaseSummary(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        return ResponseEntity.ok(new java.util.ArrayList<>(reportService.getPurchaseSummary(companyId, warehouseId, locationId, startDate, endDate)));
    }
}

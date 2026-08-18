package org.enterprise.production.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.production.dto.*;
import org.enterprise.production.service.ProductionReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/production/reports")
@RequiredArgsConstructor
public class ProductionReportController {

    private final ProductionReportService reportService;

    @GetMapping("/daily")
    public ResponseEntity<List<DailyProductionReportDto>> getDailyProductionReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getDailyProductionReport(startDate, endDate));
    }

    @GetMapping("/by-product")
    public ResponseEntity<List<ProductProductionReportDto>> getProductProductionReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getProductProductionReport(startDate, endDate));
    }

    @GetMapping("/status")
    public ResponseEntity<List<ProductionStatusReportDto>> getProductionStatusReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getProductionStatusReport(startDate, endDate));
    }

    @GetMapping("/yield")
    public ResponseEntity<List<ProductionYieldReportDto>> getProductionYieldReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getProductionYieldReport(startDate, endDate));
    }

    @GetMapping("/bom-usage")
    public ResponseEntity<List<BomUsageReportDto>> getBomUsageReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getBomUsageReport(startDate, endDate));
    }
}

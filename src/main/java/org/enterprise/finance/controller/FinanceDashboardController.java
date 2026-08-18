package org.enterprise.finance.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.dto.FinanceDashboardDTO;
import org.enterprise.finance.service.FinanceDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/dashboard")
@RequiredArgsConstructor
public class FinanceDashboardController {

    private final FinanceDashboardService financeDashboardService;

    @GetMapping("/metrics")
    public ResponseEntity<FinanceDashboardDTO> getMetrics() {
        return ResponseEntity.ok(financeDashboardService.getDashboardMetrics());
    }
}

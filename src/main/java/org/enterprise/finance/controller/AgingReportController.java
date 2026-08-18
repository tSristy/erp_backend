package org.enterprise.finance.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.dto.AgingReportLineDto;
import org.enterprise.finance.service.AgingReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/finance/aging")
@RequiredArgsConstructor
public class AgingReportController {

    private final AgingReportService agingReportService;

    @GetMapping("/customers")
    public ResponseEntity<List<AgingReportLineDto>> getCustomerAging() {
        return ResponseEntity.ok(agingReportService.getCustomerAging());
    }

    @GetMapping("/vendors")
    public ResponseEntity<List<AgingReportLineDto>> getVendorAging() {
        return ResponseEntity.ok(agingReportService.getVendorAging());
    }
}

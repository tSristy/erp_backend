package org.enterprise.finance.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.dto.LedgerRowDto;
import org.enterprise.finance.dto.TrialBalanceRowDto;
import org.enterprise.finance.service.FinancialReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/finance/reports")
@RequiredArgsConstructor
public class FinancialReportController {

    private final FinancialReportService financialReportService;

    @GetMapping("/trial-balance")
    public List<TrialBalanceRowDto> getTrialBalance(@RequestParam Long periodId) {
        return financialReportService.getTrialBalance(periodId);
    }

    @GetMapping("/gl-ledger")
    public List<LedgerRowDto> getGlLedger(
            @RequestParam String glCode,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        return financialReportService.getGlLedger(glCode, startDate, endDate);
    }

    @GetMapping("/customer-ledger")
    public List<LedgerRowDto> getCustomerLedger(
            @RequestParam String customerCode,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        return financialReportService.getCustomerLedger(customerCode, startDate, endDate);
    }

    @GetMapping("/vendor-ledger")
    public List<LedgerRowDto> getVendorLedger(
            @RequestParam String vendorCode,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        return financialReportService.getVendorLedger(vendorCode, startDate, endDate);
    }

    @GetMapping("/subledger")
    public List<LedgerRowDto> getDimensionLedger(
            @RequestParam String dimensionType,
            @RequestParam String dimensionCode,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        return financialReportService.getDimensionLedger(dimensionType, dimensionCode, startDate, endDate);
    }
}

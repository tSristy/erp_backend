package org.enterprise.finance.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.dto.FinancialStatementRowDto;
import org.enterprise.finance.enums.ReportType;
import org.enterprise.finance.service.FinancialStatementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/financial-statements")
@RequiredArgsConstructor
public class FinancialStatementController {

    private final FinancialStatementService service;

    @GetMapping
    public List<FinancialStatementRowDto> generate(
            @RequestParam ReportType reportType,
            @RequestParam Long periodId
    ) {

        return service.generateStatement(
                reportType,
                periodId
        );
    }
}
/*
GET /api/financial-statements?
reportType=INCOME_STATEMENT&
periodId=5

GET /api/financial-statements?
reportType=BALANCE_SHEET&
periodId=5

GET /api/financial-statements?
reportType=CASH_FLOW&
periodId=5

 */
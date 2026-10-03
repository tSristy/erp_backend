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
@RequestMapping("/api/v1/financial-statements")
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

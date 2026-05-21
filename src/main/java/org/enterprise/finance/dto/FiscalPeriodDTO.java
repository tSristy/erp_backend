package org.enterprise.finance.dto;

import lombok.Data;

@Data
public class FiscalPeriodDTO {
    private Long id;
    private String periodName;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private org.enterprise.finance.enums.PeriodStatus status;
    private FiscalYearDTO fiscalYear;
}

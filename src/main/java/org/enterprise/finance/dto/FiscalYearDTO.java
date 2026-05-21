package org.enterprise.finance.dto;

import lombok.Data;

@Data
public class FiscalYearDTO {
    private Long id;
    private String yearCode;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private Boolean active;
    private Boolean closed;
    private java.util.List<FiscalPeriodDTO> lines;
}

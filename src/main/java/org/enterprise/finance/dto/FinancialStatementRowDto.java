package org.enterprise.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class FinancialStatementRowDto {

    private Integer serialNo;

    private Integer parentSerialNo;

    private Integer levelNo;

    private String particulars;

    private BigDecimal amount;

    private Boolean bold;

    private Boolean bottomLine;

    private Boolean visible;
}
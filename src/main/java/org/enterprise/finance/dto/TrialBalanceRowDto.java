package org.enterprise.finance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TrialBalanceRowDto {
    private Long accountId;
    private String accountCode;
    private String accountName;
    private BigDecimal openingDebit;
    private BigDecimal openingCredit;
    private BigDecimal periodDebit;
    private BigDecimal periodCredit;
    private BigDecimal closingDebit;
    private BigDecimal closingCredit;
}

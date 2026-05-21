package org.enterprise.finance.dto;

import lombok.Data;

@Data
public class AccountBalanceDTO {
    private Long id;
    private AccountDTO account;
    private FiscalYearDTO fiscalYear;
    private FiscalPeriodDTO fiscalPeriod;
    private Long branchId;
}

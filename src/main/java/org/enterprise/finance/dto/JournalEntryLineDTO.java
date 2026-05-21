package org.enterprise.finance.dto;

import lombok.Data;

@Data
public class JournalEntryLineDTO {
    private Long id;
    private JournalEntryDTO journalEntry;
    private AccountDTO account;
    private java.math.BigDecimal debit;
    private java.math.BigDecimal credit;
    private CostCenterDTO costCenter;
    private ProfitCenterDTO profitCenter;
    private Long branchId;
}

package org.enterprise.finance.dto;

import lombok.Data;

@Data
public class FinanceDashboardDTO {
    private long totalAccounts;
    private long totalCostCenters;
    private long totalProfitCenters;
    private long totalJournalEntries;
}

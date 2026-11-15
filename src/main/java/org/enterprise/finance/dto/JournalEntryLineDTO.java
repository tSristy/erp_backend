package org.enterprise.finance.dto;

import lombok.Data;

@Data
public class JournalEntryLineDTO {
    private Long id;
    private AccountDTO account;
    private Long accountId;
    private java.math.BigDecimal debit;
    private java.math.BigDecimal credit;
    private CostCenterDTO costCenter;
    private Long costCenterId;
    private ProfitCenterDTO profitCenter;
    private Long profitCenterId;
    private IdNameDTO branch;
    private Long branchId;
    private IdNameDTO businessPartner;
    private Long businessPartnerId;
    private IdNameDTO project;
    private Long projectId;
    private IdNameDTO internalOrder;
    private Long internalOrderId;
    private IdNameDTO letterOfCredit;
    private Long letterOfCreditId;
    private IdNameDTO loan;
    private Long loanId;

    @Data
    public static class IdNameDTO {
        private Long id;
        private String code;
        private String name;
    }
}

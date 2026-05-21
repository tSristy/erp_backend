package org.enterprise.finance.dto;

import lombok.Data;

@Data
public class JournalEntryDTO {
    private Long id;
    private String journalNo;
    private java.time.LocalDate postingDate;
    private String referenceType;
    private Long referenceId;
    private org.enterprise.finance.enums.JournalStatus status;
    private java.math.BigDecimal totalDebit;
    private java.math.BigDecimal totalCredit;
    private java.util.List<JournalEntryLineDTO> lines;
}

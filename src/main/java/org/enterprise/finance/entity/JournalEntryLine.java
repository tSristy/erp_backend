package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.organization.entity.Branch;

import org.enterprise.inventory.entity.BusinessPartner;
import java.math.BigDecimal;

@Entity
@Table(name = "fin_journal_entry_lines")
@Getter
@Setter
public class JournalEntryLine extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @Column(precision = 18, scale = 2)
    private BigDecimal debit;

    @Column(precision = 18, scale = 2)
    private BigDecimal credit;

    @ManyToOne(fetch = FetchType.LAZY)
    private CostCenter costCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProfitCenter profitCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessPartner businessPartner;

    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    private InternalOrder internalOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.inventory.entity.LetterOfCredit letterOfCredit;

    @ManyToOne(fetch = FetchType.LAZY)
    private Loan loan;
}

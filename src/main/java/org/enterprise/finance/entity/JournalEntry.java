package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.finance.enums.JournalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "fin_journal_entries")
@Getter
@Setter
public class JournalEntry extends AuditableEntity {

    private String journalNo;

    private LocalDate postingDate;

    private String referenceType;

    private Long referenceId;

    @Enumerated(EnumType.STRING)
    private JournalStatus status;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalDebit;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalCredit;

    @OneToMany(mappedBy = "journalEntry",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<JournalEntryLine> lines;
}

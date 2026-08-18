package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.finance.entity.BankAccount;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pur_letter_of_credits")
@Getter
@Setter
public class LetterOfCredit extends AuditableEntity {

    @Column(unique = true)
    private String lcNumber;

    private LocalDate lcDate;
    
    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private BankAccount issuingBank;

    private String advisingBank;

    @Column(precision = 18, scale = 2)
    private BigDecimal lcValue;

    private String currency;

    @Column(precision = 18, scale = 6)
    private BigDecimal exchangeRate;

    private String portOfLoading;
    private String portOfDischarge;

    @Enumerated(EnumType.STRING)
    private LcStatus status = LcStatus.DRAFT;
    
    private String remarks;

    public enum LcStatus {
        DRAFT, OPENED, NEGOTIATED, CLOSED
    }
}

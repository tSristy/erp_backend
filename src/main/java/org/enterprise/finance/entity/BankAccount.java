package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "fin_bank_accounts")
@Getter
@Setter
public class BankAccount extends AuditableEntity {

    private String bankName;
    
    private String branchName;
    
    private String accountNumber;
    
    private String routingNumber;
    
    private String currencyCode;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_partner_id")
    private org.enterprise.inventory.entity.BusinessPartner businessPartner;

    private Boolean active = true;
}

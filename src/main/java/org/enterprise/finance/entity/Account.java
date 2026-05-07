package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.finance.enums.AccountType;

@Entity
@Table(name = "fin_accounts")
@Getter
@Setter
public class Account extends AuditableEntity {

    private String code;

    private String name;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account parent;

    private Boolean allowPosting = true;

    private Boolean active = true;
}

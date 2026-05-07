package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fin_statement_setup_accounts",
        indexes = {
                @Index(name = "idx_ssa_stmt", columnList = "statement_setup_id"),
                @Index(name = "idx_ssa_account", columnList = "account_id")
        })
@Getter
@Setter
public class StatementSetupAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private StatementSetup statementSetup;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;
}
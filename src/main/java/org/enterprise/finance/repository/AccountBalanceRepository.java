package org.enterprise.finance.repository;

import org.enterprise.finance.entity.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface AccountBalanceRepository
        extends JpaRepository<AccountBalance, Long> {

    @Query("""
        SELECT COALESCE(
            SUM(ab.closingDebit - ab.closingCredit),
            0
        )
        FROM AccountBalance ab
        WHERE ab.account.id = :accountId
        AND ab.fiscalPeriod.id = :periodId
    """)
    BigDecimal getAccountBalance(
            Long accountId,
            Long periodId
    );
}
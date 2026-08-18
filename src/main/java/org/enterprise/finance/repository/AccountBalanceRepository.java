package org.enterprise.finance.repository;

import org.enterprise.finance.entity.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface AccountBalanceRepository
        extends JpaRepository<AccountBalance, Long> {

    @Query("SELECT a FROM AccountBalance a WHERE a.account.id = :accountId AND a.fiscalPeriod.id = :periodId AND (a.branch.id = :branchId OR (a.branch IS NULL AND :branchId IS NULL))")
    Optional<AccountBalance> findByAccountAndPeriodAndBranch(
            @Param("accountId") Long accountId,
            @Param("periodId") Long periodId,
            @Param("branchId") Long branchId);

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

    @Query("SELECT a FROM AccountBalance a WHERE a.fiscalPeriod.id = :periodId AND a.account.companyId = :companyId")
    java.util.List<AccountBalance> findByFiscalPeriodIdAndCompanyId(@Param("periodId") Long periodId, @Param("companyId") Long companyId);
}
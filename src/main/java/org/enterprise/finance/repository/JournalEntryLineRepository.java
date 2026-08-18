package org.enterprise.finance.repository;

import org.enterprise.finance.entity.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long>, JpaSpecificationExecutor<JournalEntryLine> {

    @Query("SELECT l FROM JournalEntryLine l WHERE l.account.code = :accountCode AND l.journalEntry.postingDate >= :startDate AND l.journalEntry.postingDate <= :endDate AND l.journalEntry.status = org.enterprise.finance.enums.JournalStatus.POSTED ORDER BY l.journalEntry.postingDate ASC, l.id ASC")
    List<JournalEntryLine> findByAccountCodeAndDateRange(@Param("accountCode") String accountCode, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(COALESCE(l.debit, 0) - COALESCE(l.credit, 0)) FROM JournalEntryLine l WHERE l.account.code = :accountCode AND l.journalEntry.postingDate >= :startDate AND l.journalEntry.postingDate < :endDate AND l.journalEntry.status = org.enterprise.finance.enums.JournalStatus.POSTED")
    BigDecimal calculateAccountRollForwardBalance(@Param("accountCode") String accountCode, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT l FROM JournalEntryLine l WHERE l.businessPartner.code = :partnerCode AND l.journalEntry.postingDate >= :startDate AND l.journalEntry.postingDate <= :endDate AND l.journalEntry.status = org.enterprise.finance.enums.JournalStatus.POSTED ORDER BY l.journalEntry.postingDate ASC, l.id ASC")
    List<JournalEntryLine> findByBusinessPartnerCodeAndDateRange(@Param("partnerCode") String partnerCode, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(COALESCE(l.debit, 0) - COALESCE(l.credit, 0)) FROM JournalEntryLine l WHERE l.businessPartner.code = :partnerCode AND l.journalEntry.postingDate >= :startDate AND l.journalEntry.postingDate < :endDate AND l.journalEntry.status = org.enterprise.finance.enums.JournalStatus.POSTED")
    BigDecimal calculateBusinessPartnerRollForwardBalance(@Param("partnerCode") String partnerCode, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

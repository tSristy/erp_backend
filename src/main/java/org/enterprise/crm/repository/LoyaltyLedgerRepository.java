package org.enterprise.crm.repository;

import org.enterprise.crm.entity.LoyaltyLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyLedgerRepository extends JpaRepository<LoyaltyLedger, Long> {
    boolean existsByReferenceTransactionNoAndTransactionType(String referenceTransactionNo, LoyaltyLedger.TransactionType transactionType);
    java.util.List<LoyaltyLedger> findByProfile_IdOrderByTransactionDateDesc(Long profileId);
}

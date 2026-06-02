package org.enterprise.crm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.crm.entity.LoyaltyLedger;
import org.enterprise.crm.entity.LoyaltyProfile;
import org.enterprise.crm.repository.LoyaltyLedgerRepository;
import org.enterprise.crm.repository.LoyaltyProfileRepository;
import org.enterprise.inventory.entity.BusinessPartner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {

    private final LoyaltyProfileRepository profileRepository;
    private final LoyaltyLedgerRepository ledgerRepository;

    @Transactional
    public void awardPoints(Long customerId, BigDecimal amountSpent, String posTransactionNo, LocalDateTime transactionDate) {
        if (customerId == null) return;
        
        // Example logic: 1 point for every $10 spent
        BigDecimal pointsToAward = amountSpent.divide(BigDecimal.TEN, 2, java.math.RoundingMode.HALF_UP);
        if (pointsToAward.compareTo(BigDecimal.ZERO) <= 0) return;

        LoyaltyProfile profile = profileRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    LoyaltyProfile newProfile = new LoyaltyProfile();
                    BusinessPartner customer = new BusinessPartner();
                    customer.setId(customerId);
                    newProfile.setCustomer(customer);
                    return profileRepository.save(newProfile);
                });

        profile.setTotalPointsEarned(profile.getTotalPointsEarned().add(pointsToAward));
        profile.setCurrentPointsBalance(profile.getCurrentPointsBalance().add(pointsToAward));

        LoyaltyLedger ledger = new LoyaltyLedger();
        ledger.setProfile(profile);
        ledger.setTransactionType(LoyaltyLedger.TransactionType.EARNED);
        ledger.setPoints(pointsToAward);
        ledger.setReferenceTransactionNo(posTransactionNo);
        ledger.setTransactionDate(transactionDate);
        
        ledgerRepository.save(ledger);
        profileRepository.save(profile);
        
        log.info("Awarded {} points to customer {} for transaction {}", pointsToAward, customerId, posTransactionNo);
    }
}

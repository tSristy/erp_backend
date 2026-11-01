package org.enterprise.crm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.enterprise.common.util.TenantContext;
import org.enterprise.crm.entity.LoyaltyLedger;
import org.enterprise.crm.entity.LoyaltyProfile;
import org.enterprise.crm.repository.LoyaltyLedgerRepository;
import org.enterprise.crm.repository.LoyaltyProfileRepository;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.repository.BusinessPartnerRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {

    private final LoyaltyProfileRepository profileRepository;
    private final LoyaltyLedgerRepository ledgerRepository;
    private final BusinessPartnerRepository businessPartnerRepository;

    public static String calculateTier(BigDecimal totalPointsEarned) {
        if (totalPointsEarned == null) return "BRONZE";
        if (totalPointsEarned.compareTo(new BigDecimal("1000")) >= 0) {
            return "GOLD";
        }
        if (totalPointsEarned.compareTo(new BigDecimal("300")) >= 0) {
            return "SILVER";
        }
        return "BRONZE";
    }

    private int tierRank(String tier) {
        if ("GOLD".equalsIgnoreCase(tier)) return 3;
        if ("SILVER".equalsIgnoreCase(tier)) return 2;
        return 1;
    }

    private void upgradeTierIfEligible(LoyaltyProfile profile) {
        String calculated = calculateTier(profile.getTotalPointsEarned());
        if (profile.getLoyaltyTier() == null || tierRank(calculated) > tierRank(profile.getLoyaltyTier())) {
            profile.setLoyaltyTier(calculated);
        }
    }

    @Transactional(readOnly = true)
    public Optional<LoyaltyProfile> findByCustomerId(Long customerId) {
        if (customerId == null) return Optional.empty();
        return profileRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public LoyaltyProfile getOrBuildDefaultProfile(Long customerId) {
        return findByCustomerId(customerId).orElseGet(() -> {
            LoyaltyProfile empty = new LoyaltyProfile();
            empty.assignCustomerId(customerId);
            empty.setTotalPointsEarned(BigDecimal.ZERO);
            empty.setCurrentPointsBalance(BigDecimal.ZERO);
            empty.setLoyaltyTier("BRONZE");
            return empty;
        });
    }

    @Transactional
    public LoyaltyProfile saveProfile(LoyaltyProfile incoming) {
        Long companyId = TenantContext.getCompanyId();
        Long customerId = incoming.resolveCustomerId();
        if (customerId == null) {
            throw new IllegalArgumentException("Customer is required for Loyalty Profile");
        }

        BusinessPartner customer = businessPartnerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));

        LoyaltyProfile target;
        if (incoming.getId() != null) {
            target = profileRepository.findById(incoming.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Loyalty profile not found with ID: " + incoming.getId()));
        } else {
            target = profileRepository.findByCustomerId(customerId).orElseGet(LoyaltyProfile::new);
        }

        if (target.getCompanyId() == null) {
            target.setCompanyId(companyId != null ? companyId : customer.getCompanyId());
        }
        target.setCustomer(customer);
        target.setTotalPointsEarned(incoming.getTotalPointsEarned() != null ? incoming.getTotalPointsEarned() : BigDecimal.ZERO);
        target.setCurrentPointsBalance(incoming.getCurrentPointsBalance() != null ? incoming.getCurrentPointsBalance() : BigDecimal.ZERO);
        if (incoming.getLoyaltyTier() != null && !incoming.getLoyaltyTier().trim().isEmpty()) {
            target.setLoyaltyTier(incoming.getLoyaltyTier().trim().toUpperCase());
        } else {
            target.setLoyaltyTier(calculateTier(target.getTotalPointsEarned()));
        }

        return profileRepository.save(target);
    }

    @Transactional
    public void awardPoints(Long customerId, BigDecimal amountSpent, String referenceTransactionNo, LocalDateTime transactionDate) {
        if (customerId == null || amountSpent == null || amountSpent.compareTo(BigDecimal.ZERO) <= 0) return;

        if (referenceTransactionNo != null && !referenceTransactionNo.trim().isEmpty()
                && ledgerRepository.existsByReferenceTransactionNoAndTransactionType(referenceTransactionNo.trim(), LoyaltyLedger.TransactionType.EARNED)) {
            log.debug("Loyalty points already awarded for transaction {}", referenceTransactionNo);
            return;
        }

        // 1 point for every 10 currency units spent
        BigDecimal pointsToAward = amountSpent.divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
        if (pointsToAward.compareTo(BigDecimal.ZERO) <= 0) return;

        Long companyId = TenantContext.getCompanyId();
        LoyaltyProfile profile = profileRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    LoyaltyProfile newProfile = new LoyaltyProfile();
                    BusinessPartner customer = businessPartnerRepository.findById(customerId).orElseGet(() -> {
                        BusinessPartner bp = new BusinessPartner();
                        bp.setId(customerId);
                        return bp;
                    });
                    newProfile.setCustomer(customer);
                    newProfile.setCompanyId(companyId != null ? companyId : customer.getCompanyId());
                    newProfile.setTotalPointsEarned(BigDecimal.ZERO);
                    newProfile.setCurrentPointsBalance(BigDecimal.ZERO);
                    newProfile.setLoyaltyTier("BRONZE");
                    return profileRepository.save(newProfile);
                });

        BigDecimal currentEarned = profile.getTotalPointsEarned() != null ? profile.getTotalPointsEarned() : BigDecimal.ZERO;
        BigDecimal currentBalance = profile.getCurrentPointsBalance() != null ? profile.getCurrentPointsBalance() : BigDecimal.ZERO;

        profile.setTotalPointsEarned(currentEarned.add(pointsToAward));
        profile.setCurrentPointsBalance(currentBalance.add(pointsToAward));
        upgradeTierIfEligible(profile);

        LoyaltyLedger ledger = new LoyaltyLedger();
        ledger.setCompanyId(profile.getCompanyId());
        ledger.setProfile(profile);
        ledger.setTransactionType(LoyaltyLedger.TransactionType.EARNED);
        ledger.setPoints(pointsToAward);
        ledger.setReferenceTransactionNo(referenceTransactionNo);
        ledger.setTransactionDate(transactionDate != null ? transactionDate : LocalDateTime.now());

        ledgerRepository.save(ledger);
        profileRepository.save(profile);

        log.info("Awarded {} loyalty points to customer {} for transaction {}", pointsToAward, customerId, referenceTransactionNo);
    }

    @Transactional
    public void redeemPoints(Long customerId, BigDecimal pointsToRedeem, String referenceTransactionNo, LocalDateTime transactionDate) {
        if (customerId == null || pointsToRedeem == null || pointsToRedeem.compareTo(BigDecimal.ZERO) <= 0) return;

        if (referenceTransactionNo != null && !referenceTransactionNo.trim().isEmpty()
                && ledgerRepository.existsByReferenceTransactionNoAndTransactionType(referenceTransactionNo.trim(), LoyaltyLedger.TransactionType.REDEEMED)) {
            log.debug("Loyalty points already redeemed for transaction {}", referenceTransactionNo);
            return;
        }

        LoyaltyProfile profile = profileRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Customer does not have an active Loyalty Profile to redeem points."));

        BigDecimal currentBalance = profile.getCurrentPointsBalance() != null ? profile.getCurrentPointsBalance() : BigDecimal.ZERO;
        if (currentBalance.compareTo(pointsToRedeem) < 0) {
            throw new RuntimeException(String.format(
                    "Insufficient loyalty points balance. Available: %s, Requested: %s",
                    currentBalance.stripTrailingZeros().toPlainString(),
                    pointsToRedeem.stripTrailingZeros().toPlainString()
            ));
        }

        profile.setCurrentPointsBalance(currentBalance.subtract(pointsToRedeem));

        LoyaltyLedger ledger = new LoyaltyLedger();
        ledger.setCompanyId(profile.getCompanyId());
        ledger.setProfile(profile);
        ledger.setTransactionType(LoyaltyLedger.TransactionType.REDEEMED);
        ledger.setPoints(pointsToRedeem);
        ledger.setReferenceTransactionNo(referenceTransactionNo);
        ledger.setTransactionDate(transactionDate != null ? transactionDate : LocalDateTime.now());

        ledgerRepository.save(ledger);
        profileRepository.save(profile);

        log.info("Redeemed {} loyalty points from customer {} for transaction {}", pointsToRedeem, customerId, referenceTransactionNo);
    }

    @Transactional
    public void reversePointsForReturn(Long customerId, BigDecimal returnedAmount, String referenceTransactionNo, LocalDateTime transactionDate) {
        if (customerId == null || returnedAmount == null || returnedAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        if (referenceTransactionNo != null && !referenceTransactionNo.trim().isEmpty()
                && ledgerRepository.existsByReferenceTransactionNoAndTransactionType(referenceTransactionNo.trim(), LoyaltyLedger.TransactionType.REDEEMED)) {
            return;
        }

        BigDecimal pointsToReverse = returnedAmount.divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
        if (pointsToReverse.compareTo(BigDecimal.ZERO) <= 0) return;

        profileRepository.findByCustomerId(customerId).ifPresent(profile -> {
            BigDecimal currentBalance = profile.getCurrentPointsBalance() != null ? profile.getCurrentPointsBalance() : BigDecimal.ZERO;
            BigDecimal currentEarned = profile.getTotalPointsEarned() != null ? profile.getTotalPointsEarned() : BigDecimal.ZERO;

            BigDecimal deducted = currentBalance.min(pointsToReverse);
            profile.setCurrentPointsBalance(currentBalance.subtract(deducted).max(BigDecimal.ZERO));
            profile.setTotalPointsEarned(currentEarned.subtract(pointsToReverse).max(BigDecimal.ZERO));
            profile.setLoyaltyTier(calculateTier(profile.getTotalPointsEarned()));

            if (deducted.compareTo(BigDecimal.ZERO) > 0) {
                LoyaltyLedger ledger = new LoyaltyLedger();
                ledger.setCompanyId(profile.getCompanyId());
                ledger.setProfile(profile);
                ledger.setTransactionType(LoyaltyLedger.TransactionType.REDEEMED);
                ledger.setPoints(deducted);
                ledger.setReferenceTransactionNo(referenceTransactionNo);
                ledger.setTransactionDate(transactionDate != null ? transactionDate : LocalDateTime.now());
                ledgerRepository.save(ledger);
            }
            profileRepository.save(profile);
            log.info("Reversed {} loyalty points for customer {} on return {}", deducted, customerId, referenceTransactionNo);
        });
    }

    @EventListener
    @Transactional
    public void handlePosTransactionCompleted(PosTransactionCompletedEvent event) {
        if (event == null || event.getCustomerId() == null) {
            return;
        }

        Long customerId = event.getCustomerId();
        String txNo = event.getTransactionNo();
        LocalDateTime txDate = event.getTransactionDate() != null ? event.getTransactionDate() : LocalDateTime.now();
        String txType = event.getTransactionType() != null ? event.getTransactionType().toUpperCase() : "SALES";

        if ("RETURN".equals(txType)) {
            reversePointsForReturn(customerId, event.getTotalAmount(), txNo, txDate);
            return;
        }

        BigDecimal loyaltyPaid = BigDecimal.ZERO;
        if (event.getPayments() != null) {
            for (PosTransactionCompletedEvent.PaymentDto payment : event.getPayments()) {
                if (payment != null && "LOYALTY_POINTS".equalsIgnoreCase(payment.getPaymentMode()) && payment.getAmount() != null) {
                    loyaltyPaid = loyaltyPaid.add(payment.getAmount().max(BigDecimal.ZERO));
                }
            }
        }

        if (loyaltyPaid.compareTo(BigDecimal.ZERO) > 0) {
            redeemPoints(customerId, loyaltyPaid, txNo, txDate);
        }

        BigDecimal totalAmount = event.getTotalAmount() != null ? event.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal netEligibleSpend = totalAmount.subtract(loyaltyPaid).max(BigDecimal.ZERO);
        if (netEligibleSpend.compareTo(BigDecimal.ZERO) > 0) {
            awardPoints(customerId, netEligibleSpend, txNo, txDate);
        }
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LoyaltyProfile> searchLoyaltyProfiles(String query, org.springframework.data.domain.Pageable pageable) {
        return profileRepository.findAll(org.enterprise.crm.specification.LoyaltyProfileSpecification.searchByQuery(query), pageable);
    }
}

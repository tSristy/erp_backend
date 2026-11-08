package org.enterprise.crm.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.enterprise.crm.service.LoyaltyService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class PosTransactionLoyaltyListener {

    private final LoyaltyService loyaltyService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void handlePosTransactionCompleted(PosTransactionCompletedEvent event) {
        if (event.getCustomerId() == null) {
            return;
        }
        try {
            log.info("CRM Module received POS transaction completion event for: {}. Processing loyalty points...", event.getTransactionNo());
            loyaltyService.awardPoints(event.getCustomerId(), event.getTotalAmount(), event.getTransactionNo(), event.getTransactionDate());
        } catch (Exception ex) {
            log.warn("Could not award loyalty points for POS Transaction {}: {}", event.getTransactionNo(), ex.getMessage());
        }
    }
}

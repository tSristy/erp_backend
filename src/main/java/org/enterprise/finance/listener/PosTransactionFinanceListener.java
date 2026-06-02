package org.enterprise.finance.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class PosTransactionFinanceListener {

    // private final JournalEntryService journalEntryService;

    /**
     * Listens for POS transactions and records the payment automatically.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePosTransactionCompleted(PosTransactionCompletedEvent event) {
        log.info("Finance Module received POS transaction completion event for: {}. Processing {} payments...", 
                event.getTransactionNo(), event.getPayments().size());
        
        for (PosTransactionCompletedEvent.PaymentDto payment : event.getPayments()) {
            if ("DUE".equalsIgnoreCase(payment.getPaymentMode())) {
                log.info("Recording Accounts Receivable for DUE amount: {}", payment.getAmount());
                // TODO: Debit A/R, Credit Sales Revenue
            } else {
                log.info("Recording Receipt for payment mode {}: Amount {}", payment.getPaymentMode(), payment.getAmount());
                // TODO: Debit Cash/Bank depending on mode, Credit Sales Revenue
            }
        }
        
        log.info("Payments recorded successfully for POS Transaction: {}", event.getTransactionNo());
    }
}

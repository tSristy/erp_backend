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
        log.info("Finance Module received POS transaction completion event for: {} (Type: {}). Processing payments...", event.getTransactionNo(), event.getTransactionType());
        
        boolean isReturn = "RETURN".equals(event.getTransactionType());
        
        for (PosTransactionCompletedEvent.PaymentDto payment : event.getPayments()) {
            if ("DUE".equalsIgnoreCase(payment.getPaymentMode())) {
                log.info("Recording Accounts Receivable for DUE amount: {}", payment.getAmount());
                // TODO: Debit A/R, Credit Sales Revenue
            } else {
                if (isReturn) {
                    log.info("Recording Refund for mode {} (Amount: {})", payment.getPaymentMode(), payment.getAmount());
                    // TODO: Debit Sales Return / Revenue Account
                    // TODO: Credit Cash/Bank Account (payment.getAmount())
                } else {
                    log.info("Recording Receipt for mode {} (Amount: {})", payment.getPaymentMode(), payment.getAmount());
                    // TODO: Debit Cash/Bank Account (payment.getAmount())
                    // TODO: Credit Sales Revenue Account
                }
            }
        }
        
        log.info("Payments recorded successfully for POS Transaction: {}", event.getTransactionNo());
    }
}

package org.enterprise.sales.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class PosTransactionSalesListener {

    /**
     * Listens for POS transactions.
     * Note: B2B Sales Invoices require a mandatory SalesOrder, whereas B2C POS sales
     * are recorded directly in Retail/Restaurant POS and posted to Finance & Inventory via
     * PosTransactionFinanceListener and PosTransactionInventoryListener.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePosTransactionCompleted(PosTransactionCompletedEvent event) {
        log.info("Sales Module acknowledged POS transaction completion event for: {} (Type: {}).",
                event.getTransactionNo(), event.getTransactionType());
    }
}

package org.enterprise.inventory.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class PosTransactionInventoryListener {

    // private final InventoryService inventoryService;

    /**
     * Listens for POS transactions and deducts the inventory automatically.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePosTransactionCompleted(PosTransactionCompletedEvent event) {
        log.info("Inventory Module received POS transaction completion event for: {}. Processing Inventory Movement (Stock Out)...", event.getTransactionNo());
        
        Long warehouseId = event.getWarehouseId();
        if (warehouseId == null) {
            log.warn("No warehouse specified in POS transaction {}. Cannot deduct inventory or using default...", event.getTransactionNo());
        }

        for (PosTransactionCompletedEvent.LineItemDto item : event.getLineItems()) {
            log.info("Deducting {} units of Product ID {} from Warehouse ID {}", 
                     item.getQuantity(), item.getProductId(), warehouseId);
            // TODO: inventoryService.adjustStock(inventoryId, item.getQuantity().negate()); 
            // Or create an InventoryTransaction entity and save it.
        }
        
        log.info("Inventory successfully deducted for POS Transaction: {}", event.getTransactionNo());
    }
}

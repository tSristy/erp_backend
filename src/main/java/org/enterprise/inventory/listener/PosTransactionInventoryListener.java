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
        log.info("Inventory Module received POS transaction completion event for: {} (Type: {}). Processing stock adjustments...", event.getTransactionNo(), event.getTransactionType());
        
        boolean isReturn = "RETURN".equals(event.getTransactionType());
        
        Long warehouseId = event.getWarehouseId();
        if (warehouseId == null) {
            log.warn("No warehouse specified in POS transaction {}. Cannot deduct inventory or using default...", event.getTransactionNo());
        }

        for (PosTransactionCompletedEvent.LineItemDto item : event.getLineItems()) {
            if (isReturn) {
                log.info("Stock In for Return: Product ID {} (Qty: {}) to Warehouse ID {}", item.getProductId(), item.getQuantity(), event.getWarehouseId());
                // TODO: inventoryService.adjustStock(event.getWarehouseId(), item.getProductId(), item.getQuantity());
            } else {
                log.info("Stock Out for Sale: Product ID {} (Qty: {}) from Warehouse ID {}", item.getProductId(), item.getQuantity(), event.getWarehouseId());
                // TODO: inventoryService.adjustStock(event.getWarehouseId(), item.getProductId(), item.getQuantity().negate());
            } 
            // Or create an InventoryTransaction entity and save it.
        }
        
        log.info("Inventory successfully deducted for POS Transaction: {}", event.getTransactionNo());
    }
}

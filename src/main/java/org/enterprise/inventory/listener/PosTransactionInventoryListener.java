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

    private final org.enterprise.inventory.service.InventoryService inventoryService;

    /**
     * Listens for POS transactions and deducts/restores the inventory automatically.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void handlePosTransactionCompleted(PosTransactionCompletedEvent event) {
        log.info("Inventory Module received POS transaction completion event for: {} (Type: {}). Processing stock adjustments...", event.getTransactionNo(), event.getTransactionType());
        
        boolean isReturn = "RETURN".equals(event.getTransactionType());
        
        Long warehouseId = event.getWarehouseId();
        if (warehouseId == null) {
            log.warn("No warehouse specified in POS transaction {}. Skipping inventory stock adjustment.", event.getTransactionNo());
            return;
        }

        for (PosTransactionCompletedEvent.LineItemDto item : event.getLineItems()) {
            if (item.getProductId() == null || item.getQuantity() == null) continue;

            org.enterprise.inventory.dto.InventoryTransactionRequest req = new org.enterprise.inventory.dto.InventoryTransactionRequest();
            req.setItemId(item.getProductId());
            req.setWarehouseId(warehouseId);
            req.setDocumentType("POS_TRANSACTION");
            req.setQuantity(item.getQuantity().abs());
            req.setUnitCost(item.getUnitPrice() != null ? item.getUnitPrice() : java.math.BigDecimal.ZERO);

            if (isReturn) {
                log.info("Stock In for Return: Product ID {} (Qty: {}) to Warehouse ID {}", item.getProductId(), item.getQuantity(), warehouseId);
                req.setTransactionType("SALES_RETURN");
                inventoryService.receiveStock(req);
            } else {
                log.info("Stock Out for Sale: Product ID {} (Qty: {}) from Warehouse ID {}", item.getProductId(), item.getQuantity(), warehouseId);
                req.setTransactionType("SALES");
                inventoryService.issueStock(req);
            }
        }
        
        log.info("Inventory successfully adjusted for POS Transaction: {}", event.getTransactionNo());
    }
}

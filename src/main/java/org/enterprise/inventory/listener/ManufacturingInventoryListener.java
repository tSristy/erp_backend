package org.enterprise.inventory.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.ManufacturingOrderCompletedEvent;
import org.enterprise.inventory.entity.InventoryLedger;
import org.enterprise.inventory.enums.InventoryTransactionType;
import org.enterprise.inventory.repository.InventoryLedgerRepository;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.entity.Warehouse;
import org.enterprise.inventory.repository.ProductRepository;
import org.enterprise.inventory.repository.WarehouseRepository;
import org.enterprise.production.entity.BomItem;
import org.enterprise.production.repository.BomItemRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ManufacturingInventoryListener {

    private final InventoryLedgerRepository inventoryLedgerRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final BomItemRepository bomItemRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleManufacturingOrderCompleted(ManufacturingOrderCompletedEvent event) {
        log.info("Inventory Module received Manufacturing Order completion event for: {}. Processing stock movements...", event.getOrderNo());

        Warehouse warehouse = warehouseRepository.findById(event.getProductionWarehouseId()).orElse(null);
        if (warehouse == null) {
            log.error("Warehouse not found for manufacturing order {}", event.getOrderNo());
            return;
        }

        // 1. Stock OUT for Raw Materials (BOM Items)
        List<BomItem> bomItems = bomItemRepository.findByBomId(event.getBomId());
        for (BomItem item : bomItems) {
            Product rawMaterial = item.getRawMaterial();
            // Total consumed = bom quantity per FG * total FG produced
            BigDecimal consumedQty = item.getQuantity().multiply(event.getProducedQuantity());

            InventoryLedger outTx = new InventoryLedger();
            outTx.setTransactionType(InventoryTransactionType.PRODUCTION_ISSUE);
            outTx.setDocumentType("MANUFACTURING_CONSUMPTION");
            outTx.setProduct(rawMaterial);
            outTx.setWarehouse(warehouse);
            outTx.setQtyOut(consumedQty);
            outTx.setTransactionDate(event.getCompletionDate());
            
            inventoryLedgerRepository.save(outTx);
            log.info("Consumed {} of {} for order {}", consumedQty, rawMaterial.getName(), event.getOrderNo());
        }

        // 2. Stock IN for Finished Good (FG/SFG)
        Product finishedGood = productRepository.findById(event.getFinishedGoodId()).orElse(null);
        if (finishedGood != null) {
            InventoryLedger inTx = new InventoryLedger();
            inTx.setTransactionType(InventoryTransactionType.PRODUCTION_RECEIVE);
            inTx.setDocumentType("MANUFACTURING_PRODUCTION");
            inTx.setProduct(finishedGood);
            inTx.setWarehouse(warehouse);
            inTx.setQtyIn(event.getProducedQuantity());
            inTx.setTransactionDate(event.getCompletionDate());
            
            inventoryLedgerRepository.save(inTx);
            log.info("Produced {} of {} for order {}", event.getProducedQuantity(), finishedGood.getName(), event.getOrderNo());
        }
    }
}

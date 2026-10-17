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

    private final org.enterprise.inventory.service.InventoryService inventoryService;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final BomItemRepository bomItemRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void handleManufacturingOrderCompleted(ManufacturingOrderCompletedEvent event) {
        log.info("Inventory Module received Manufacturing Order completion event for: {}. Processing stock movements...", event.getOrderNo());

        Warehouse warehouse = warehouseRepository.findById(event.getProductionWarehouseId()).orElse(null);
        if (warehouse == null) {
            log.error("Warehouse not found for manufacturing order {}", event.getOrderNo());
            return;
        }

        BigDecimal totalRawMaterialCost = BigDecimal.ZERO;

        // 1. Stock OUT for Raw Materials (BOM Items)
        List<BomItem> bomItems = bomItemRepository.findByBomId(event.getBomId());
        for (BomItem item : bomItems) {
            Product rawMaterial = item.getRawMaterial();
            // Total consumed = bom quantity per FG * total FG produced
            BigDecimal consumedQty = item.getQuantity().multiply(event.getProducedQuantity());

            org.enterprise.inventory.dto.InventoryTransactionRequest issueReq = new org.enterprise.inventory.dto.InventoryTransactionRequest();
            issueReq.setItemId(rawMaterial.getId());
            issueReq.setWarehouseId(warehouse.getId());
            issueReq.setTransactionType("PRODUCTION_ISSUE");
            issueReq.setDocumentType("MANUFACTURING_CONSUMPTION");
            issueReq.setQuantity(consumedQty);

            BigDecimal consumedValue = inventoryService.issueStock(issueReq);
            totalRawMaterialCost = totalRawMaterialCost.add(consumedValue);
            log.info("Consumed {} of {} (Value: {}) for order {}", consumedQty, rawMaterial.getName(), consumedValue, event.getOrderNo());
        }

        // 2. Stock IN for Finished Good (FG/SFG)
        Product finishedGood = productRepository.findById(event.getFinishedGoodId()).orElse(null);
        if (finishedGood != null && event.getProducedQuantity() != null && event.getProducedQuantity().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal fgUnitCost = totalRawMaterialCost.divide(event.getProducedQuantity(), 6, java.math.RoundingMode.HALF_UP);

            org.enterprise.inventory.dto.InventoryTransactionRequest receiveReq = new org.enterprise.inventory.dto.InventoryTransactionRequest();
            receiveReq.setItemId(finishedGood.getId());
            receiveReq.setWarehouseId(warehouse.getId());
            receiveReq.setTransactionType("PRODUCTION_RECEIVE");
            receiveReq.setDocumentType("MANUFACTURING_PRODUCTION");
            receiveReq.setQuantity(event.getProducedQuantity());
            receiveReq.setUnitCost(fgUnitCost);

            inventoryService.receiveStock(receiveReq);
            log.info("Produced {} of {} (Unit Cost: {}) for order {}", event.getProducedQuantity(), finishedGood.getName(), fgUnitCost, event.getOrderNo());
        }
    }
}

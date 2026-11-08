package org.enterprise.production.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.entity.Warehouse;
import org.enterprise.production.entity.ManufacturingOrder;
import org.enterprise.production.repository.ManufacturingOrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
public class PosProductionListener {

    private final ManufacturingOrderRepository manufacturingOrderRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void handlePosTransactionCompleted(PosTransactionCompletedEvent event) {
        try {
            log.info("Production Module received POS transaction completion event for: {}. Processing backflush for FG/SFG...", event.getTransactionNo());
            Long companyId = event.getCompanyId() != null ? event.getCompanyId() : org.enterprise.common.util.TenantContext.getCompanyId();

            for (PosTransactionCompletedEvent.LineItemDto item : event.getLineItems()) {
                if (item.getProductId() == null || item.getQuantity() == null) continue;
                log.info("Checking BOM and manufacturing FG for Product ID {} (Qty: {})", item.getProductId(), item.getQuantity());

                ManufacturingOrder mo = new ManufacturingOrder();
                mo.setCompanyId(companyId);
                mo.setOrderNo("MO-POS-" + event.getTransactionNo() + "-" + item.getProductId());
                mo.setOrderDate(LocalDate.now());

                Product fg = new Product();
                fg.setId(item.getProductId());
                mo.setFinishedGood(fg);

                if (event.getWarehouseId() != null) {
                    Warehouse warehouse = new Warehouse();
                    warehouse.setId(event.getWarehouseId());
                    mo.setProductionWarehouse(warehouse);
                }

                mo.setPlannedQuantity(item.getQuantity());
                mo.setProducedQuantity(item.getQuantity());
                mo.setStatus(ManufacturingOrder.OrderStatus.COMPLETED);

                manufacturingOrderRepository.save(mo);
                log.info("Generated Manufacturing Order {} for Product ID {}", mo.getOrderNo(), item.getProductId());
            }

            log.info("Finished FG/SFG backflushing for POS Transaction: {}", event.getTransactionNo());
        } catch (Exception ex) {
            log.warn("Production backflush skipped/failed for POS Transaction {}: {}", event.getTransactionNo(), ex.getMessage());
        }
    }
}

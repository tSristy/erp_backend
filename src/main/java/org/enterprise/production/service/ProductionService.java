package org.enterprise.production.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.service.JournalService;
import org.enterprise.inventory.entity.InventoryLedger;
import org.enterprise.inventory.entity.StockBalance;
import org.enterprise.inventory.entity.Warehouse;
import org.enterprise.inventory.enums.InventoryTransactionType;
import org.enterprise.inventory.repository.InventoryLedgerRepository;
import org.enterprise.inventory.repository.StockBalanceRepository;
import org.enterprise.inventory.service.CostingService;
import org.enterprise.production.entity.BomItem;
import org.enterprise.production.entity.ManufacturingOrder;
import org.enterprise.production.repository.ManufacturingOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductionService {

    private final ManufacturingOrderRepository orderRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final InventoryLedgerRepository inventoryLedgerRepository;
    private final JournalService journalService;
    private final CostingService costingService;

    @Transactional
    public void completeProduction(Long orderId, BigDecimal producedQty) {
        ManufacturingOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == ManufacturingOrder.OrderStatus.COMPLETED) {
            throw new RuntimeException("Order is already completed");
        }

        BigDecimal factor = producedQty.divide(order.getBom().getBaseQuantity(), 6, RoundingMode.HALF_UP);
        BigDecimal totalRawMaterialCost = BigDecimal.ZERO;

        // 1. Consume Raw Materials
        for (BomItem item : order.getBom().getItems()) {
            BigDecimal consumeQty = item.getQuantity().multiply(factor);

            StockBalance rmStock = stockBalanceRepository
                    .findByProductIdAndWarehouseIdAndLocationId(item.getRawMaterial().getId(),
                            order.getProductionWarehouse().getId(), null)
                    .orElseThrow(() -> new RuntimeException(
                            "Raw material stock not found: " + item.getRawMaterial().getName()));

            if (rmStock.getQuantity().compareTo(consumeQty) < 0) {
                throw new RuntimeException("Insufficient raw material: " + item.getRawMaterial().getName());
            }

            // Consume Cost
            BigDecimal consumeValue = costingService.consumeCost(item.getRawMaterial(), order.getProductionWarehouse(),
                    consumeQty);
            BigDecimal avgCost = consumeValue.divide(consumeQty, 6, RoundingMode.HALF_UP);

            totalRawMaterialCost = totalRawMaterialCost.add(consumeValue);

            rmStock.setQuantity(rmStock.getQuantity().subtract(consumeQty));
            rmStock.setTotalValue(rmStock.getTotalValue().subtract(consumeValue));

            if (rmStock.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
                rmStock.setAverageCost(BigDecimal.ZERO);
                rmStock.setTotalValue(BigDecimal.ZERO);
            }

            stockBalanceRepository.save(rmStock);

            // Create ledger entry for consumption
            InventoryLedger issueLedger = new InventoryLedger();
            issueLedger.setTransactionType(InventoryTransactionType.PRODUCTION_ISSUE);
            issueLedger.setDocumentType("MO");
            issueLedger.setDocumentId(order.getId());
            issueLedger.setTransactionDate(LocalDateTime.now());
            issueLedger.setWarehouse(order.getProductionWarehouse());
            issueLedger.setProduct(item.getRawMaterial());

            issueLedger.setQtyIn(BigDecimal.ZERO);
            issueLedger.setQtyOut(consumeQty);
            issueLedger.setUnitCost(avgCost);
            issueLedger.setTotalCost(consumeValue);
            issueLedger.setBalanceQty(rmStock.getQuantity());
            issueLedger.setBalanceCost(rmStock.getTotalValue());

            inventoryLedgerRepository.save(issueLedger);
        }

        // 2. Receive Finished Goods
        StockBalance fgStock = stockBalanceRepository
                .findByProductIdAndWarehouseIdAndLocationId(order.getFinishedGood().getId(),
                        order.getProductionWarehouse().getId(), null)
                .orElseGet(StockBalance::new);

        fgStock.setProduct(order.getFinishedGood());
        fgStock.setWarehouse(order.getProductionWarehouse());

        BigDecimal currentQty = Optional.ofNullable(fgStock.getQuantity()).orElse(BigDecimal.ZERO);
        BigDecimal currentTotalValue = Optional.ofNullable(fgStock.getTotalValue()).orElse(BigDecimal.ZERO);

        BigDecimal newQty = currentQty.add(producedQty);
        BigDecimal newTotalValue = currentTotalValue.add(totalRawMaterialCost);

        fgStock.setQuantity(newQty);
        fgStock.setTotalValue(newTotalValue);
        fgStock.setAverageCost(newTotalValue.divide(newQty, 6, RoundingMode.HALF_UP));

        stockBalanceRepository.save(fgStock);

        // Create ledger entry for receipt
        InventoryLedger receiveLedger = new InventoryLedger();
        receiveLedger.setTransactionType(InventoryTransactionType.PRODUCTION_RECEIVE);
        receiveLedger.setDocumentType("MO");
        receiveLedger.setDocumentId(order.getId());
        receiveLedger.setTransactionDate(LocalDateTime.now());
        receiveLedger.setWarehouse(order.getProductionWarehouse());
        receiveLedger.setProduct(order.getFinishedGood());

        BigDecimal fgUnitCost = totalRawMaterialCost.divide(producedQty, 6, RoundingMode.HALF_UP);

        receiveLedger.setQtyIn(producedQty);
        receiveLedger.setQtyOut(BigDecimal.ZERO);
        receiveLedger.setUnitCost(fgUnitCost);
        receiveLedger.setTotalCost(totalRawMaterialCost);
        receiveLedger.setBalanceQty(newQty);
        receiveLedger.setBalanceCost(newTotalValue);

        inventoryLedgerRepository.save(receiveLedger);

        // Add Cost Layer for FG
        costingService.addCostLayer(order.getFinishedGood(), order.getProductionWarehouse(), "MANUFACTURING_ORDER",
                order.getId(), producedQty, fgUnitCost);

        // 3. Post Accounting Entries
        createAccountingEntry(order, totalRawMaterialCost);

        // 4. Update Order Status
        order.setProducedQuantity(order.getProducedQuantity().add(producedQty));
        if (order.getProducedQuantity().compareTo(order.getPlannedQuantity()) >= 0) {
            order.setStatus(ManufacturingOrder.OrderStatus.COMPLETED);
        } else {
            order.setStatus(ManufacturingOrder.OrderStatus.IN_PROGRESS);
        }
        orderRepository.save(order);
    }

    private void createAccountingEntry(ManufacturingOrder order, BigDecimal totalRawMaterialCost) {
        if (totalRawMaterialCost.compareTo(BigDecimal.ZERO) <= 0)
            return;

        Warehouse warehouse = order.getProductionWarehouse();
        if (warehouse.getInventoryAccount() == null || warehouse.getWipAccount() == null) {
            throw new RuntimeException("Inventory or WIP account missing on Warehouse");
        }

        JournalEntry journal = new JournalEntry();
        journal.setPostingDate(LocalDate.now());
        journal.setReferenceType("MANUFACTURING_ORDER");
        journal.setReferenceId(order.getId());
        journal.setStatus(JournalStatus.POSTED);

        List<JournalEntryLine> lines = new ArrayList<>();

        // 1. Issue Materials (Dr WIP, Cr Raw Material Inventory)
        JournalEntryLine drWipLine = new JournalEntryLine();
        drWipLine.setJournalEntry(journal);
        drWipLine.setAccount(warehouse.getWipAccount());
        drWipLine.setDebit(totalRawMaterialCost);
        drWipLine.setCredit(BigDecimal.ZERO);
        lines.add(drWipLine);

        JournalEntryLine crRmLine = new JournalEntryLine();
        crRmLine.setJournalEntry(journal);
        crRmLine.setAccount(warehouse.getInventoryAccount());
        crRmLine.setDebit(BigDecimal.ZERO);
        crRmLine.setCredit(totalRawMaterialCost);
        lines.add(crRmLine);

        // 2. Receive Finished Goods (Dr FG Inventory, Cr WIP)
        JournalEntryLine drFgLine = new JournalEntryLine();
        drFgLine.setJournalEntry(journal);
        drFgLine.setAccount(warehouse.getInventoryAccount());
        drFgLine.setDebit(totalRawMaterialCost);
        drFgLine.setCredit(BigDecimal.ZERO);
        lines.add(drFgLine);

        JournalEntryLine crWipLine = new JournalEntryLine();
        crWipLine.setJournalEntry(journal);
        crWipLine.setAccount(warehouse.getWipAccount());
        crWipLine.setDebit(BigDecimal.ZERO);
        crWipLine.setCredit(totalRawMaterialCost);
        lines.add(crWipLine);

        journal.setLines(lines);
        journalService.save(journal);
    }
}

package org.enterprise.production.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.service.JournalEntryService;
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
    private final JournalEntryService journalEntryService;
    private final org.enterprise.finance.repository.AccountRepository accountRepository;
    private final CostingService costingService;
    private final org.enterprise.inventory.service.BatchSerialTrackingService batchSerialTrackingService;
    private final org.enterprise.production.repository.QualityInspectionRepository qualityInspectionRepository;

    @Transactional
    public void completeProduction(Long orderId, BigDecimal producedQty) {
        completeProduction(orderId, producedQty, null, null, null, null, null);
    }

    @Transactional
    public void completeProduction(
            Long orderId,
            BigDecimal producedQty,
            BigDecimal rejectedQty,
            Long qcInspectorId,
            String qcNotes) {
        completeProduction(orderId, producedQty, rejectedQty, qcInspectorId, qcNotes, null, null);
    }

    @Transactional
    public void completeProduction(
            Long orderId,
            BigDecimal producedQty,
            BigDecimal rejectedQty,
            Long qcInspectorId,
            String qcNotes,
            List<String> serialNumbers,
            String batchNo) {
        ManufacturingOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == ManufacturingOrder.OrderStatus.COMPLETED) {
            throw new RuntimeException("Order is already completed");
        }

        if (producedQty == null || producedQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Produced (accepted) quantity must be greater than zero");
        }

        BigDecimal rejected = Optional.ofNullable(rejectedQty).orElse(BigDecimal.ZERO);
        if (rejected.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Rejected quantity cannot be negative");
        }

        // Total batch manufactured (good units + rejected scrap units) consumes raw materials
        BigDecimal totalManufacturedQty = producedQty.add(rejected);
        BigDecimal factor = totalManufacturedQty.divide(order.getBom().getBaseQuantity(), 6, RoundingMode.HALF_UP);
        BigDecimal totalRawMaterialCost = BigDecimal.ZERO;

        // 1. Consume Raw Materials
        for (BomItem item : order.getBom().getItems()) {
            BigDecimal consumeQty = item.getQuantity().multiply(factor);

            StockBalance rmStock = stockBalanceRepository
                    .findByProductIdAndWarehouseIdAndLocationIdAndBatchIsNull(
                            item.getRawMaterial().getId(),
                            order.getProductionWarehouse().getId(),
                            null)
                    .or(() -> stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchId(
                            item.getRawMaterial().getId(),
                            order.getProductionWarehouse().getId(),
                            null,
                            null))
                    .or(() -> stockBalanceRepository
                            .findAvailableBatchesForIssue(item.getRawMaterial().getId(), order.getProductionWarehouse().getId())
                            .stream()
                            .findFirst())
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
            issueLedger.setCompanyId(order.getCompanyId());
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

        // 2. Receive Finished Goods (resolve Batch if batch-managed)
        if (Boolean.TRUE.equals(order.getFinishedGood().getIsBatchManaged())) {
            String effectiveBatchNo = (batchNo != null && !batchNo.trim().isEmpty())
                    ? batchNo.trim()
                    : (order.getBatch() != null ? order.getBatch().getBatchNo() : "BATCH-" + order.getOrderNo());
            org.enterprise.inventory.entity.Batch resolvedBatch = batchSerialTrackingService.findOrCreateBatch(
                    order.getFinishedGood(), effectiveBatchNo, LocalDate.now(), null);
            order.setBatch(resolvedBatch);
        }

        StockBalance fgStock;
        if (order.getBatch() != null) {
            fgStock = stockBalanceRepository
                    .findByProductIdAndWarehouseIdAndLocationIdAndBatchId(order.getFinishedGood().getId(),
                            order.getProductionWarehouse().getId(), null, order.getBatch().getId())
                    .orElseGet(StockBalance::new);
        } else {
            fgStock = stockBalanceRepository
                    .findByProductIdAndWarehouseIdAndLocationIdAndBatchIsNull(order.getFinishedGood().getId(),
                            order.getProductionWarehouse().getId(), null)
                    .orElseGet(StockBalance::new);
        }

        fgStock.setCompanyId(order.getCompanyId());
        fgStock.setProduct(order.getFinishedGood());
        fgStock.setWarehouse(order.getProductionWarehouse());
        fgStock.setBatch(order.getBatch());

        BigDecimal currentQty = Optional.ofNullable(fgStock.getQuantity()).orElse(BigDecimal.ZERO);
        BigDecimal currentTotalValue = Optional.ofNullable(fgStock.getTotalValue()).orElse(BigDecimal.ZERO);

        BigDecimal newQty = currentQty.add(producedQty);
        BigDecimal newTotalValue = currentTotalValue.add(totalRawMaterialCost);

        fgStock.setQuantity(newQty);
        fgStock.setTotalValue(newTotalValue);
        fgStock.setAverageCost(newTotalValue.divide(newQty, 6, RoundingMode.HALF_UP));

        stockBalanceRepository.save(fgStock);

        List<String> effectiveSerials = (serialNumbers != null && !serialNumbers.isEmpty())
                ? serialNumbers.stream()
                        .filter(java.util.Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
                : (order.getSerialNumbers() != null ? order.getSerialNumbers() : List.of());

        batchSerialTrackingService.validateSerialNumbers(order.getFinishedGood(), effectiveSerials, producedQty.intValue());
        batchSerialTrackingService.processInboundSerials(order.getFinishedGood(), order.getBatch(), effectiveSerials, order.getProductionWarehouse(), null, InventoryTransactionType.PRODUCTION_RECEIVE, "MO", order.getId());

        if (!effectiveSerials.isEmpty()) {
            if (order.getSerialNumbers() == null) {
                order.setSerialNumbers(new ArrayList<>());
            }
            for (String s : effectiveSerials) {
                if (!order.getSerialNumbers().contains(s)) {
                    order.getSerialNumbers().add(s);
                }
            }
        }

        // Create ledger entry for receipt
        InventoryLedger receiveLedger = new InventoryLedger();
        receiveLedger.setCompanyId(order.getCompanyId());
        receiveLedger.setTransactionType(InventoryTransactionType.PRODUCTION_RECEIVE);
        receiveLedger.setDocumentType("MO");
        receiveLedger.setDocumentId(order.getId());
        receiveLedger.setTransactionDate(LocalDateTime.now());
        receiveLedger.setWarehouse(order.getProductionWarehouse());
        receiveLedger.setProduct(order.getFinishedGood());
        receiveLedger.setBatch(order.getBatch());

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

        // 4. Update Order Status & QC Summary
        order.setProducedQuantity(Optional.ofNullable(order.getProducedQuantity()).orElse(BigDecimal.ZERO).add(producedQty));
        order.setQcPassedQuantity(Optional.ofNullable(order.getQcPassedQuantity()).orElse(BigDecimal.ZERO).add(producedQty));
        order.setQcRejectedQuantity(Optional.ofNullable(order.getQcRejectedQuantity()).orElse(BigDecimal.ZERO).add(rejected));
        if (qcInspectorId != null) {
            order.setQcInspectorId(qcInspectorId);
        }
        if (qcNotes != null && !qcNotes.trim().isEmpty()) {
            order.setQcNotes(qcNotes.trim());
        }
        if (rejected.compareTo(BigDecimal.ZERO) > 0) {
            order.setQcStatus(ManufacturingOrder.QcStatus.PARTIALLY_PASSED);
        } else if (order.getQcStatus() == null || order.getQcStatus() == ManufacturingOrder.QcStatus.PENDING) {
            order.setQcStatus(ManufacturingOrder.QcStatus.PASSED);
        }

        if (order.getProducedQuantity().compareTo(order.getPlannedQuantity()) >= 0) {
            order.setStatus(ManufacturingOrder.OrderStatus.COMPLETED);
        } else {
            order.setStatus(ManufacturingOrder.OrderStatus.IN_PROGRESS);
        }
        orderRepository.save(order);

        // 5. Record QualityInspection audit log if QC params or completion occurred
        org.enterprise.production.entity.QualityInspection inspection = new org.enterprise.production.entity.QualityInspection();
        String nextNo = qualityInspectionRepository.findTopByOrderByIdDesc()
                .map(last -> String.format("QC-%04d", last.getId() + 1))
                .orElse("QC-0001");
        inspection.setInspectionNo(nextNo);
        inspection.setInspectionDate(LocalDate.now());
        inspection.setManufacturingOrder(order);
        inspection.setInspectorId(qcInspectorId != null ? qcInspectorId : order.getQcInspectorId());
        inspection.setInspectedQuantity(totalManufacturedQty);
        inspection.setPassedQuantity(producedQty);
        inspection.setRejectedQuantity(rejected);
        inspection.setReworkQuantity(BigDecimal.ZERO);
        inspection.setStatus(rejected.compareTo(BigDecimal.ZERO) > 0
                ? ManufacturingOrder.QcStatus.PARTIALLY_PASSED
                : ManufacturingOrder.QcStatus.PASSED);
        inspection.setRemarks(qcNotes != null && !qcNotes.trim().isEmpty()
                ? qcNotes.trim()
                : "Auto-recorded upon production completion");
        inspection.setCompanyId(order.getCompanyId());
        qualityInspectionRepository.save(inspection);
    }

    private void createAccountingEntry(ManufacturingOrder order, BigDecimal totalRawMaterialCost) {
        if (totalRawMaterialCost.compareTo(BigDecimal.ZERO) <= 0)
            return;

        Long companyId = order.getCompanyId() != null
                ? order.getCompanyId()
                : org.enterprise.common.util.TenantContext.getCompanyId();

        Warehouse warehouse = order.getProductionWarehouse();
        org.enterprise.finance.entity.Account inventoryAccount = warehouse != null && warehouse.getInventoryAccount() != null
                ? warehouse.getInventoryAccount()
                : (order.getFinishedGood() != null && order.getFinishedGood().getInventoryAccount() != null
                        ? order.getFinishedGood().getInventoryAccount()
                        : (companyId != null ? accountRepository.findByCodeAndCompanyId("1030", companyId) : null));

        org.enterprise.finance.entity.Account wipAccount = warehouse != null && warehouse.getWipAccount() != null
                ? warehouse.getWipAccount()
                : inventoryAccount;

        if (inventoryAccount == null || wipAccount == null) {
            return;
        }

        JournalEntry journal = new JournalEntry();
        journal.setCompanyId(companyId);
        journal.setPostingDate(LocalDate.now());
        journal.setReferenceType("MANUFACTURING_ORDER");
        journal.setReferenceId(order.getId());
        journal.setStatus(JournalStatus.POSTED);

        List<JournalEntryLine> lines = new ArrayList<>();

        // 1. Issue Materials (Dr WIP, Cr Raw Material Inventory)
        JournalEntryLine drWipLine = new JournalEntryLine();
        drWipLine.setCompanyId(companyId);
        drWipLine.setJournalEntry(journal);
        drWipLine.setAccount(wipAccount);
        drWipLine.setDebit(totalRawMaterialCost);
        drWipLine.setCredit(BigDecimal.ZERO);
        lines.add(drWipLine);

        JournalEntryLine crRmLine = new JournalEntryLine();
        crRmLine.setCompanyId(companyId);
        crRmLine.setJournalEntry(journal);
        crRmLine.setAccount(inventoryAccount);
        crRmLine.setDebit(BigDecimal.ZERO);
        crRmLine.setCredit(totalRawMaterialCost);
        lines.add(crRmLine);

        // 2. Receive Finished Goods (Dr FG Inventory, Cr WIP)
        JournalEntryLine drFgLine = new JournalEntryLine();
        drFgLine.setCompanyId(companyId);
        drFgLine.setJournalEntry(journal);
        drFgLine.setAccount(inventoryAccount);
        drFgLine.setDebit(totalRawMaterialCost);
        drFgLine.setCredit(BigDecimal.ZERO);
        lines.add(drFgLine);

        JournalEntryLine crWipLine = new JournalEntryLine();
        crWipLine.setCompanyId(companyId);
        crWipLine.setJournalEntry(journal);
        crWipLine.setAccount(wipAccount);
        crWipLine.setDebit(BigDecimal.ZERO);
        crWipLine.setCredit(totalRawMaterialCost);
        lines.add(crWipLine);

        journal.setLines(lines);
        journalEntryService.save(journal);
    }

    public org.springframework.data.domain.Page<ManufacturingOrder> searchProductions(Long companyId, String searchTerm, org.springframework.data.domain.Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

}